package com.realive.serviceimpl.admin.user;

import com.realive.domain.common.enums.ReviewReportStatus;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.product.Product;
import com.realive.domain.review.ReviewReport;
import com.realive.domain.seller.SellerReview; // SellerReview 엔티티 경로 확인
import com.realive.domain.seller.Seller;     // Seller 엔티티 경로 확인
import com.realive.dto.admin.review.AdminReviewReportDetailDTO;
import com.realive.dto.admin.review.AdminReviewReportListItemDTO;
import com.realive.dto.admin.review.TakeActionOnReportRequestDTO;
import com.realive.dto.admin.review.AdminSellerReviewDetailDTO;
import com.realive.dto.admin.review.AdminSellerReviewListItemDTO; // 이 DTO 사용
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.review.ReviewReportRepository;
import com.realive.repository.review.SellerReviewRepository;
// SellerReviewSpecification의 정확한 import 경로를 확인해주세요.
import com.realive.repository.review.SellerReviewSpecification;
import com.realive.service.admin.user.AdminReviewService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Specification import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewReportRepository reviewReportRepository;
    private final SellerReviewRepository sellerReviewRepository;
    private final CustomerRepository customerRepository;

    // --- 기존 4개 메소드는 이전 최종본과 동일 (getReportedReviewsByStatus, getReportDetail, getSellerReviewDetail, processReportAction) ---
    // convertToAdminReviewReportListItemDTO는 AdminReviewReportListItemDTO의 reason 필드 변경 반영됨

    @Override
    public Page<AdminReviewReportListItemDTO> getReportedReviewsByStatus(ReviewReportStatus status, Pageable pageable) {
        log.info("Fetching reported reviews with status: {} and pageable: {}", status, pageable);
        Page<ReviewReport> reportPage = reviewReportRepository.findAllByStatus(status, pageable);
        List<AdminReviewReportListItemDTO> dtoList = reportPage.getContent().stream()
                .map(this::convertToAdminReviewReportListItemDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, reportPage.getTotalElements());
    }

    private AdminReviewReportListItemDTO convertToAdminReviewReportListItemDTO(ReviewReport report) {
        Long reportedReviewIdAsLong = null;
        if (report.getSellerReviewId() != null) {
            reportedReviewIdAsLong = report.getSellerReviewId().longValue();
        }
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
                .reason(report.getReason()) // DTO 필드명 'reason'으로 변경됨
                .reportedAt(report.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public AdminSellerReviewDetailDTO getSellerReviewDetail(Long reviewId) throws EntityNotFoundException {
        log.info("Fetching detail for seller review with ID: {}", reviewId);
        SellerReview review = sellerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("SellerReview not found with id: " + reviewId));
        Long orderId = null;
        String productName = "상품 정보 없음";
        Long productId = null;
        if (review.getOrder() != null) {
            Order order = review.getOrder();
            orderId = order.getId();
            if (order.getProduct() != null) {
                Product product = order.getProduct();
                productName = product.getName();
                productId = product.getId();
            }
        }
        String sellerName = (review.getSeller() != null) ? review.getSeller().getName() : "판매자 정보 없음";
        Long sellerId = (review.getSeller() != null) ? review.getSeller().getId() : null;
        String customerName = (review.getCustomer() != null) ? review.getCustomer().getName() : "고객 정보 없음";
        Long customerId = (review.getCustomer() != null) ? review.getCustomer().getId() : null;
        List<String> imageUrls = Collections.emptyList();
        Boolean isHidden = null; // SellerReview 엔티티에 isHidden 필드가 없다면 null
        String adminMemo = null;
        return AdminSellerReviewDetailDTO.builder()
                .reviewId(review.getId())
                .orderId(orderId)
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
                .isHidden(isHidden)
                .adminMemo(adminMemo)
                .build();
    }

    @Override
    @Transactional
    public AdminReviewReportDetailDTO getReportDetail(Integer reportId) throws EntityNotFoundException {
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

    @Override
    @Transactional
    public void processReportAction(Integer reportId, TakeActionOnReportRequestDTO actionRequest) throws EntityNotFoundException {
        log.info("Processing action for review report ID: {}. New status: {}", reportId, actionRequest.getNewStatus());
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("ReviewReport not found with id: " + reportId));
        report.updateStatus(actionRequest.getNewStatus());
        reviewReportRepository.save(report);
        log.info("ReviewReport (ID: {}) status updated to: {}", report.getId(), report.getStatus());
    }

    // === 이 메소드가 수정되었습니다 (sellerFilter 파라미터 및 로직 추가) ===
    @Override
    public Page<AdminSellerReviewListItemDTO> getAllSellerReviews(Pageable pageable,
                                                                  Optional<String> productFilter,
                                                                  Optional<String> customerFilter,
                                                                  Optional<String> sellerFilter) { // sellerFilter 파라미터 받음
        log.info("Fetching all seller reviews. Pageable: {}, ProductFilter: {}, CustomerFilter: {}, SellerFilter: {}",
                pageable, productFilter.orElse("N/A"), customerFilter.orElse("N/A"), sellerFilter.orElse("N/A"));

        Specification<SellerReview> spec = Specification.where(null); // 기본 Specification

        if (productFilter.isPresent() && !productFilter.get().isBlank()) {
            spec = spec.and(SellerReviewSpecification.productNameContains(productFilter.get()));
        }

        if (customerFilter.isPresent() && !customerFilter.get().isBlank()) {
            spec = spec.and(SellerReviewSpecification.customerNameContains(customerFilter.get()));
        }

        if (sellerFilter.isPresent() && !sellerFilter.get().isBlank()) { // sellerFilter 조건 추가
            spec = spec.and(SellerReviewSpecification.sellerNameContains(sellerFilter.get()));
        }

        Page<SellerReview> sellerReviewPage = sellerReviewRepository.findAll(spec, pageable);

        List<AdminSellerReviewListItemDTO> dtoList = sellerReviewPage.getContent().stream()
                .map(this::convertToAdminSellerReviewListItemDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, sellerReviewPage.getTotalElements());
    }

    // AdminSellerReviewListItemDTO 변환 메소드 (이전과 동일)
    private AdminSellerReviewListItemDTO convertToAdminSellerReviewListItemDTO(SellerReview review) {
        String productName = (review.getOrder() != null && review.getOrder().getProduct() != null)
                ? review.getOrder().getProduct().getName() : "상품 정보 없음";
        Long productId = (review.getOrder() != null && review.getOrder().getProduct() != null)
                ? review.getOrder().getProduct().getId() : null;
        String customerName = (review.getCustomer() != null) ? review.getCustomer().getName() : "고객 정보 없음";
        Long customerId = (review.getCustomer() != null) ? review.getCustomer().getId() : null;
        String sellerName = (review.getSeller() != null) ? review.getSeller().getName() : "판매자 정보 없음";
        Long sellerId = (review.getSeller() != null) ? review.getSeller().getId() : null;

        String contentSummary = review.getContent();
        if (contentSummary != null && contentSummary.length() > 50) {
            contentSummary = contentSummary.substring(0, 50) + "...";
        }

        Boolean isHiddenValue = null; // SellerReview 엔티티에 isHidden 필드가 없다면 항상 null
        // if (review.getIsHidden() != null) { // SellerReview 엔티티에 isHidden 필드가 있다면
        //     isHiddenValue = review.getIsHidden();
        // }

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
                .isHidden(isHiddenValue)
                .build();
    }

    // --- 인터페이스에서 주석 처리된 나머지 메소드들의 구현부는 여기서도 주석 처리 또는 삭제 ---
    /*
    @Override
    @Transactional
    public void updateSellerReviewVisibility(Long reviewId, Boolean isHidden) throws EntityNotFoundException {
        // TODO: 구현 필요
    }

    @Override
    @Transactional
    public void deleteSellerReview(Long reviewId) throws EntityNotFoundException {
        // TODO: 구현 필요
    }
    */
}
