package com.realive.serviceimpl.admin.user;

import com.realive.domain.common.enums.ReviewReportStatus;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderItem; // OrderItem 엔티티 import
import com.realive.domain.product.Product; // Product 엔티티 import
import com.realive.domain.review.ReviewReport;
import com.realive.domain.seller.SellerReview;
import com.realive.dto.admin.review.*;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderItemRepository; // OrderItemRepository import
import com.realive.repository.review.ReviewReportRepository;
import com.realive.repository.review.SellerReviewRepository;
import com.realive.repository.review.SellerReviewSpecification;
import com.realive.service.admin.user.AdminReviewService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*; // HashMap, Map import
import java.util.stream.Collectors;

/**
 * AdminReviewService 인터페이스의 구현체입니다.
 * (Javadoc 주석은 이전 최종본과 유사하게 유지)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewReportRepository reviewReportRepository;
    private final SellerReviewRepository sellerReviewRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository; // OrderItemRepository 주입

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<AdminReviewReportListItemDTO> getReportedReviewsByStatus(ReviewReportStatus status, Pageable pageable) {
        // ... (이전 최종본과 동일)
        log.info("Fetching reported reviews with status: {} and pageable: {}", status, pageable);
        Page<ReviewReport> reportPage = reviewReportRepository.findAllByStatus(status, pageable);
        List<AdminReviewReportListItemDTO> dtoList = reportPage.getContent().stream()
                .map(this::convertToAdminReviewReportListItemDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, reportPage.getTotalElements());
    }

    private AdminReviewReportListItemDTO convertToAdminReviewReportListItemDTO(ReviewReport report) {
        // ... (이전 최종본과 동일)
        Long reportedReviewIdAsLong = (report.getSellerReviewId() != null) ? report.getSellerReviewId().longValue() : null;
        String reporterName = "신고자 정보 없음";
        Long reporterIdAsLong = null;
        if (report.getReporterId() != null) {
            reporterIdAsLong = report.getReporterId().longValue();
            Optional<Customer> customerOpt = customerRepository.findById(reporterIdAsLong);
            if (customerOpt.isPresent()) {
                reporterName = customerOpt.get().getName();
            }
        }
        return AdminReviewReportListItemDTO.builder()
                .reportId(report.getId())
                .status(report.getStatus())
                .reportedReviewId(reportedReviewIdAsLong)
                .reporterId(reporterIdAsLong)
                .reporterName(reporterName)
                .reason(report.getReason())
                .reportedAt(report.getCreatedAt())
                .build();
    }

    /**
     * {@inheritDoc}
     * 이 메소드는 SellerReview 엔티티의 isHidden 필드 값을 DTO에 포함하여 반환하며,
     * OrderItemRepository를 통해 관련된 상품 정보를 조회하여 DTO에 포함합니다.
     */
    @Override
    public AdminSellerReviewDetailDTO getSellerReviewDetail(Long reviewId) throws EntityNotFoundException {
        log.info("Fetching detail for seller review with ID: {}", reviewId);
        SellerReview review = sellerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("SellerReview not found with id: " + reviewId));

        Long orderIdFromReview = null;
        String productName = "상품 정보 없음";
        Long productId = null;

        if (review.getOrder() != null) {
            Order order = review.getOrder();
            orderIdFromReview = order.getId();

            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            if (orderItems != null && !orderItems.isEmpty()) {
                OrderItem firstOrderItem = orderItems.get(0); // 첫 번째 주문 항목의 상품을 대표로 가정
                if (firstOrderItem.getProduct() != null) {
                    Product product = firstOrderItem.getProduct();
                    productName = product.getName();
                    productId = product.getId();
                }
            }
        }

        String sellerName = (review.getSeller() != null) ? review.getSeller().getName() : "판매자 정보 없음";
        Long sellerId = (review.getSeller() != null) ? review.getSeller().getId() : null;
        String customerName = (review.getCustomer() != null) ? review.getCustomer().getName() : "고객 정보 없음";
        Long customerId = (review.getCustomer() != null) ? review.getCustomer().getId() : null;
        List<String> imageUrls = Collections.emptyList();

        return AdminSellerReviewDetailDTO.builder()
                .reviewId(review.getId())
                .orderId(orderIdFromReview)
                .productName(productName)
                .productId(productId)
                .sellerName(sellerName)
                .sellerId(sellerId)
                .customerName(customerName)
                .customerId(customerId)
                .rating(review.getRating())
                .imageUrls(imageUrls)
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .isHidden(review.getIsHidden())
                .build();
    }

    /**
     * {@inheritDoc}
     * 신고 대상 리뷰의 상세 정보 조회 시, 해당 리뷰의 isHidden 상태 및 관련 상품 정보도 함께 포함됩니다.
     */
    @Override
    public AdminReviewReportDetailDTO getReportDetail(Integer reportId) throws EntityNotFoundException {
        // ... (내부 getSellerReviewDetail 호출 부분은 이전 최종본과 동일, 상품 정보 포함됨)
        log.info("Fetching detail for review report with ID: {}", reportId);
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("ReviewReport not found with id: " + reportId));
        AdminSellerReviewDetailDTO reportedReviewDetailContent = null;
        if (report.getSellerReviewId() != null) {
            try {
                reportedReviewDetailContent = this.getSellerReviewDetail(report.getSellerReviewId().longValue());
            } catch (EntityNotFoundException e) {
                log.warn("Associated SellerReview (ID: {}) not found for ReviewReport (ID: {}). Error: {}",
                        report.getSellerReviewId(), reportId, e.getMessage());
            }
        }
        Long reporterIdAsLong = null;
        String reporterName = "신고자 정보 없음";
        String reporterEmail = null;
        if (report.getReporterId() != null) {
            reporterIdAsLong = report.getReporterId().longValue();
            Optional<Customer> customerOpt = customerRepository.findById(reporterIdAsLong);
            if (customerOpt.isPresent()) {
                Customer reporter = customerOpt.get();
                reporterName = reporter.getName();
                reporterEmail = reporter.getEmail();
            }
        }
        return AdminReviewReportDetailDTO.builder()
                .reportId(report.getId())
                .status(report.getStatus())
                .reason(report.getReason())
                .reportedAt(report.getCreatedAt())
                .reportUpdatedAt(report.getUpdatedAt())
                .reporterId(reporterIdAsLong)
                .reporterName(reporterName)
                .reporterEmail(reporterEmail)
                .reportedReviewDetail(reportedReviewDetailContent)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void processReportAction(Integer reportId, TakeActionOnReportRequestDTO actionRequest) throws EntityNotFoundException {
        // ... (이전 최종본과 동일)
        log.info("Processing action for review report ID: {}. New status: {}", reportId, actionRequest.getNewStatus());
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("ReviewReport not found with id: " + reportId));
        report.updateStatus(actionRequest.getNewStatus());
        reviewReportRepository.save(report);
        log.info("ReviewReport (ID: {}) status updated to: {}", report.getId(), report.getStatus());
    }

    /**
     * {@inheritDoc}
     * N+1 문제 해결을 위해, 리뷰 목록 조회 후 관련 OrderItem 정보를 일괄 조회하여
     * 각 리뷰의 상품 정보를 효율적으로 DTO에 매핑합니다.
     */
    @Override
    public Page<AdminSellerReviewListItemDTO> getAllSellerReviews(Pageable pageable,
                                                                  Optional<String> productFilter,
                                                                  Optional<String> customerFilter,
                                                                  Optional<String> sellerFilter) {
        log.info("Fetching all seller reviews. Pageable: {}, ProductFilter: {}, CustomerFilter: {}, SellerFilter: {}",
                pageable, productFilter.orElse("N/A"), customerFilter.orElse("N/A"), sellerFilter.orElse("N/A"));

        Specification<SellerReview> spec = Specification.where(null);
        if (productFilter.isPresent() && !productFilter.get().isBlank()) {
            spec = spec.and(SellerReviewSpecification.productNameContains(productFilter.get()));
        }
        if (customerFilter.isPresent() && !customerFilter.get().isBlank()) {
            spec = spec.and(SellerReviewSpecification.customerNameContains(customerFilter.get()));
        }
        if (sellerFilter.isPresent() && !sellerFilter.get().isBlank()) {
            spec = spec.and(SellerReviewSpecification.sellerNameContains(sellerFilter.get()));
        }

        Page<SellerReview> sellerReviewPage = sellerReviewRepository.findAll(spec, pageable);

        // N+1 문제 해결: OrderItem 정보를 미리 가져오기
        List<Long> orderIds = sellerReviewPage.getContent().stream()
                .filter(review -> review.getOrder() != null)
                .map(review -> review.getOrder().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> orderIdToProductMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderIdIn(orderIds); // IN 절 사용
            for (OrderItem item : orderItems) {
                if (item.getOrder() != null && item.getProduct() != null) {
                    // 주문 ID별 첫 번째 상품 정보만 저장 (정책에 따라 변경 가능)
                    orderIdToProductMap.putIfAbsent(item.getOrder().getId(), item.getProduct());
                }
            }
        }

        List<AdminSellerReviewListItemDTO> dtoList = sellerReviewPage.getContent().stream()
                .map(review -> {
                    Product product = null;
                    if (review.getOrder() != null && orderIdToProductMap.containsKey(review.getOrder().getId())) {
                        product = orderIdToProductMap.get(review.getOrder().getId());
                    }
                    return convertToAdminSellerReviewListItemDTO(review, product); // Product 정보 전달
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, sellerReviewPage.getTotalElements());
    }

    /** SellerReview 엔티티와 Product 엔티티를 AdminSellerReviewListItemDTO로 변환합니다. */
    private AdminSellerReviewListItemDTO convertToAdminSellerReviewListItemDTO(SellerReview review, Product product) {
        String productName = (product != null) ? product.getName() : "상품 정보 없음";
        Long productId = (product != null) ? product.getId() : null;

        String customerName = (review.getCustomer() != null) ? review.getCustomer().getName() : "고객 정보 없음";
        Long customerId = (review.getCustomer() != null) ? review.getCustomer().getId() : null;
        String sellerName = (review.getSeller() != null) ? review.getSeller().getName() : "판매자 정보 없음";
        Long sellerId = (review.getSeller() != null) ? review.getSeller().getId() : null;
        String contentSummary = review.getContent();
        if (contentSummary != null && contentSummary.length() > 50) {
            contentSummary = contentSummary.substring(0, 50) + "...";
        }

        return AdminSellerReviewListItemDTO.builder()
                .reviewId(review.getId())
                .productName(productName)
                .productId(productId)
                .customerName(customerName)
                .customerId(customerId)
                .sellerName(sellerName)
                .sellerId(sellerId)
                .rating(review.getRating())
                .contentSummary(contentSummary)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .isHidden(review.getIsHidden())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional // DB 업데이트 (쓰기 작업)
    public void updateSellerReviewVisibility(Long reviewId, Boolean isHidden) throws EntityNotFoundException {
        log.info("Attempting to update visibility for seller review ID: {}. New isHidden status: {}", reviewId, isHidden);
        SellerReview review = sellerReviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("SellerReview not found for visibility update with ID: {}", reviewId);
                    return new EntityNotFoundException("SellerReview not found with id: " + reviewId);
                });
        review.setIsHidden(isHidden);
        sellerReviewRepository.save(review);
        log.info("Successfully updated visibility for seller review ID: {} to isHidden: {}", reviewId, isHidden);
    }
}
