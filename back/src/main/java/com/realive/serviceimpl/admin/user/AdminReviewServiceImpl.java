package com.realive.serviceimpl.admin.user;

import com.realive.domain.common.enums.ReviewReportStatus;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.product.Product;
import com.realive.domain.review.ReviewReport;
import com.realive.domain.seller.SellerReview;
import com.realive.domain.seller.Seller;
import com.realive.dto.admin.review.*;
import com.realive.repository.customer.CustomerRepository;
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

    @Override
    public Page<AdminReviewReportListItemDTO> getReportedReviewsByStatus(ReviewReportStatus status, Pageable pageable) {
        log.info("Fetching reported reviews with status: {} and pageable: {}", status, pageable);
        Page<ReviewReport> reportPage = reviewReportRepository.findAllByStatus(status, pageable); // Page<ReviewReport> 타입 명시
        List<AdminReviewReportListItemDTO> dtoList = reportPage.getContent().stream() // List<AdminReviewReportListItemDTO> 타입 명시
                .map(this::convertToAdminReviewReportListItemDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, reportPage.getTotalElements());
    } // getReportedReviewsByStatus 메소드 닫는 중괄호

    private AdminReviewReportListItemDTO convertToAdminReviewReportListItemDTO(ReviewReport report) {
        Long reportedReviewIdAsLong = null;
        if (report.getSellerReviewId() != null) {
            reportedReviewIdAsLong = report.getSellerReviewId().longValue();
        } // if 닫는 중괄호

        String reporterName = "신고자 정보 없음";
        Long reporterIdAsLong = null;
        if (report.getReporterId() != null) {
            reporterIdAsLong = report.getReporterId().longValue();
            Optional<Customer> customerOpt = customerRepository.findById(reporterIdAsLong); // Optional<Customer> 타입 명시
            if (customerOpt.isPresent()) {
                reporterName = customerOpt.get().getName();
            } // if 닫는 중괄호
        } // if 닫는 중괄호

        return AdminReviewReportListItemDTO.builder()
                .reportId(report.getId())
                .status(report.getStatus())
                .reportedReviewId(reportedReviewIdAsLong)
                .reporterId(reporterIdAsLong)
                .reporterName(reporterName)
                .reason(report.getReason())
                .reportedAt(report.getCreatedAt())
                .build();
    } // convertToAdminReviewReportListItemDTO 메소드 닫는 중괄호

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
            } // 안쪽 if 닫는 중괄호
        } // 바깥쪽 if 닫는 중괄호

        String sellerName = (review.getSeller() != null) ? review.getSeller().getName() : "판매자 정보 없음";
        Long sellerId = (review.getSeller() != null) ? review.getSeller().getId() : null;
        String customerName = (review.getCustomer() != null) ? review.getCustomer().getName() : "고객 정보 없음";
        Long customerId = (review.getCustomer() != null) ? review.getCustomer().getId() : null;
        List<String> imageUrls = Collections.emptyList(); // List<String> 타입 명시
        Boolean isHidden = null;
        String adminMemo = null; // 사용자 첨부 파일에 누락된 부분 복원
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
    } // getSellerReviewDetail 메소드 닫는 중괄호

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
            } // catch 닫는 중괄호
        } // if 닫는 중괄호

        Long reporterIdAsLong = null;
        String reporterName = "신고자 정보 없음";
        String reporterEmail = null;
        if (report.getReporterId() != null) {
            reporterIdAsLong = report.getReporterId().longValue();
            Optional<Customer> customerOpt = customerRepository.findById(reporterIdAsLong); // Optional<Customer> 타입 명시
            if (customerOpt.isPresent()) {
                Customer reporter = customerOpt.get();
                reporterName = reporter.getName();
                reporterEmail = reporter.getEmail();
            } // 안쪽 if 닫는 중괄호
        } // 바깥쪽 if 닫는 중괄호

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
    } // getReportDetail 메소드 닫는 중괄호

    @Override
    @Transactional
    public void processReportAction(Integer reportId, TakeActionOnReportRequestDTO actionRequest) throws EntityNotFoundException {
        log.info("Processing action for review report ID: {}. New status: {}", reportId, actionRequest.getNewStatus());
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("ReviewReport not found with id: " + reportId));
        report.updateStatus(actionRequest.getNewStatus());
        reviewReportRepository.save(report);
        log.info("ReviewReport (ID: {}) status updated to: {}", report.getId(), report.getStatus());
    } // processReportAction 메소드 닫는 중괄호

    @Override
    public Page<AdminSellerReviewListItemDTO> getAllSellerReviews(Pageable pageable,
                                                                  Optional<String> productFilter,
                                                                  Optional<String> customerFilter,
                                                                  Optional<String> sellerFilter) {
        log.info("Fetching all seller reviews. Pageable: {}, ProductFilter: {}, CustomerFilter: {}, SellerFilter: {}",
                pageable, productFilter.orElse("N/A"), customerFilter.orElse("N/A"), sellerFilter.orElse("N/A"));

        Specification<SellerReview> spec = Specification.where(null); // Specification<SellerReview> 타입 명시

        if (productFilter.isPresent() && !productFilter.get().isBlank()) {
            spec = spec.and(SellerReviewSpecification.productNameContains(productFilter.get()));
        } // if 닫는 중괄호

        if (customerFilter.isPresent() && !customerFilter.get().isBlank()) {
            spec = spec.and(SellerReviewSpecification.customerNameContains(customerFilter.get()));
        } // if 닫는 중괄호

        if (sellerFilter.isPresent() && !sellerFilter.get().isBlank()) {
            spec = spec.and(SellerReviewSpecification.sellerNameContains(sellerFilter.get()));
        } // if 닫는 중괄호

        Page<SellerReview> sellerReviewPage = sellerReviewRepository.findAll(spec, pageable); // Page<SellerReview> 타입 명시
        List<AdminSellerReviewListItemDTO> dtoList = sellerReviewPage.getContent().stream() // List<AdminSellerReviewListItemDTO> 타입 명시
                .map(this::convertToAdminSellerReviewListItemDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, sellerReviewPage.getTotalElements());
    } // getAllSellerReviews 메소드 닫는 중괄호

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
        } // if 닫는 중괄호

        Boolean isHiddenValue = null;
        if (review.getIsHidden() != null) { // SellerReview 엔티티에 isHidden 필드가 있다고 가정하고 주석 해제
            isHiddenValue = review.getIsHidden();
        } // if 닫는 중괄호

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
    } // convertToAdminSellerReviewListItemDTO 메소드 닫는 중괄호

    // === "리뷰 삭제 API"를 위한 메소드 구현 추가 ===
    @Override
    @Transactional
    public void deleteSellerReview(Long reviewId) throws EntityNotFoundException {
        log.info("Attempting to delete seller review with ID: {}", reviewId);
        if (!sellerReviewRepository.existsById(reviewId)) {
            log.warn("SellerReview not found for deletion with ID: {}", reviewId);
            throw new EntityNotFoundException("SellerReview not found with id: " + reviewId);
        }
        sellerReviewRepository.deleteById(reviewId);
        log.info("Successfully deleted seller review with ID: {}", reviewId);
    } // deleteSellerReview 메소드 닫는 중괄호

    // --- 아직 구현하지 않은 다른 기능의 뼈대는 주석 처리 또는 삭제 ---
    /*
    @Override
    @Transactional
    public void updateSellerReviewVisibility(Long reviewId, Boolean isHidden) throws EntityNotFoundException {
        // TODO: 구현 필요
    }
    */
}
