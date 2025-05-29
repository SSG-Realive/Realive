//// 경로: com/realive/serviceimpl/admin/user/AdminReviewServiceImpl.java
//package com.realive.serviceimpl.admin.user; // 사용자님이 사용하시는 패키지 경로
//
//import com.realive.domain.common.enums.ReviewReportStatus;
//import com.realive.domain.customer.Customer;
//import com.realive.domain.order.Order;
//import com.realive.domain.product.Product;
//import com.realive.domain.review.ReviewReport;
//import com.realive.domain.seller.SellerReview;
//import com.realive.domain.seller.Seller;
//import com.realive.dto.admin.review.AdminReviewReportDetailDTO; // DTO 경로 일치
//import com.realive.dto.admin.review.AdminReviewReportListItemDTO; // DTO 경로 일치
//import com.realive.dto.admin.review.AdminSellerReviewDetailDTO;   // DTO 경로 일치
//import com.realive.repository.customer.CustomerRepository;
//import com.realive.repository.review.ReviewReportRepository; // 패키지명은 'report'가 더 적절할 수 있음
//import com.realive.repository.review.SellerReviewRepository;
//import com.realive.service.admin.user.AdminReviewService; // 인터페이스 경로 일치
//
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class AdminReviewServiceImpl implements AdminReviewService { // 인터페이스 구현
//
//    private final ReviewReportRepository reviewReportRepository;
//    private final SellerReviewRepository sellerReviewRepository;
//    private final CustomerRepository customerRepository;
//    // OrderRepository, ProductRepository, SellerRepository 등은
//    // SellerReview 엔티티가 연관 객체를 직접 참조하고 LAZY 로딩으로 접근 가능하므로,
//    // 이 서비스에 직접 주입할 필요는 없습니다.
//
//    @Override
//    public Page<AdminReviewReportListItemDTO> getReportedReviewsByStatus(ReviewReportStatus status, Pageable pageable) {
//        log.info("Fetching reported reviews with status: {} and pageable: {}", status, pageable);
//        Page<ReviewReport> reportPage = reviewReportRepository.findAllByStatus(status, pageable);
//
//        List<AdminReviewReportListItemDTO> dtoList = reportPage.getContent().stream()
//                .map(this::convertToAdminReviewReportListItemDTO)
//                .collect(Collectors.toList());
//
//        return new PageImpl<>(dtoList, pageable, reportPage.getTotalElements());
//    }
//
//    private AdminReviewReportListItemDTO convertToAdminReviewReportListItemDTO(ReviewReport report) {
//        Long reportedReviewIdAsLong = null;
//        if (report.getSellerReviewId() != null) {
//            reportedReviewIdAsLong = report.getSellerReviewId().longValue();
//        }
//
//        String reporterName = "신고자 정보 없음";
//        Long reporterIdAsLong = null;
//        if (report.getReporterId() != null) {
//            reporterIdAsLong = report.getReporterId().longValue();
//            Optional<Customer> customerOpt = customerRepository.findById(reporterIdAsLong);
//            if (customerOpt.isPresent()) {
//                reporterName = customerOpt.get().getName();
//            }
//        }
//
//        String reasonSummary = report.getReason();
//        if (reasonSummary != null && reasonSummary.length() > 50) {
//            reasonSummary = reasonSummary.substring(0, 50) + "...";
//        }
//
//        return AdminReviewReportListItemDTO.builder()
//                .reportId(report.getId())
//                .status(report.getStatus())
//                .reportedReviewId(reportedReviewIdAsLong)
//                // .reportedReviewContentSummary() 필드는 DTO에서 제거됨
//                .reporterId(reporterIdAsLong)
//                .reporterName(reporterName)
//                .reasonSummary(reasonSummary)
//                .reportedAt(report.getCreatedAt())
//                .build();
//    }
//
//    @Override
//    @Transactional // LAZY 로딩을 위해 트랜잭션 필요
//    public AdminSellerReviewDetailDTO getSellerReviewDetail(Long reviewId) throws EntityNotFoundException {
//        log.info("Fetching detail for seller review with ID: {}", reviewId);
//        SellerReview review = sellerReviewRepository.findById(reviewId)
//                .orElseThrow(() -> new EntityNotFoundException("SellerReview not found with id: " + reviewId));
//
//        Long orderId = null;
//        String productName = "상품 정보 없음";
//        Long productId = null;
//
//        if (review.getOrder() != null) {
//            Order order = review.getOrder();
//            orderId = order.getId();
//            if (order.getProduct() != null) {
//                Product product = order.getProduct();
//                productName = product.getName();
//                productId = product.getId();
//            }
//        }
//
//        String sellerName = (review.getSeller() != null) ? review.getSeller().getName() : "판매자 정보 없음";
//        Long sellerId = (review.getSeller() != null) ? review.getSeller().getId() : null;
//
//        String customerName = (review.getCustomer() != null) ? review.getCustomer().getName() : "고객 정보 없음";
//        Long customerId = (review.getCustomer() != null) ? review.getCustomer().getId() : null;
//
//        List<String> imageUrls = Collections.emptyList(); // SellerReview 엔티티에 이미지 정보 없음
//        Boolean isHidden = null;     // SellerReview 엔티티에 숨김 정보 없음
//        String adminMemo = null;      // SellerReview 엔티티에 관리자 메모 정보 없음
//
//        return AdminSellerReviewDetailDTO.builder()
//                .reviewId(review.getId())
//                .orderId(orderId)
//                .productName(productName)
//                .productId(productId)
//                .sellerName(sellerName)
//                .sellerId(sellerId)
//                .customerName(customerName)
//                .customerId(customerId)
//                .rating(review.getRating())
//                .imageUrls(imageUrls)
//                .content(review.getContent())
//                .createdAt(review.getCreatedAt())
//                .updatedAt(review.getUpdatedAt())
//                .isHidden(isHidden)
//                .adminMemo(adminMemo)
//                .build();
//    }
//
//    @Override // 이 어노테이션이 에러 없이 인식되어야 합니다.
//    @Transactional // LAZY 로딩 및 getSellerReviewDetail 호출을 위해 트랜잭션 필요
//    public AdminReviewReportDetailDTO getReportDetail(Integer reportId) throws EntityNotFoundException {
//        log.info("Fetching detail for review report with ID: {}", reportId);
//        ReviewReport report = reviewReportRepository.findById(reportId)
//                .orElseThrow(() -> new EntityNotFoundException("ReviewReport not found with id: " + reportId));
//
//        AdminSellerReviewDetailDTO reportedReviewDetailContent = null; // 변수명 변경
//        if (report.getSellerReviewId() != null) {
//            try {
//                reportedReviewDetailContent = this.getSellerReviewDetail(report.getSellerReviewId().longValue());
//            } catch (EntityNotFoundException e) {
//                log.warn("Associated SellerReview (ID: {}) not found for ReviewReport (ID: {}). Error: {}",
//                        report.getSellerReviewId(), reportId, e.getMessage());
//            }
//        }
//
//        Long reporterIdAsLong = null;
//        String reporterName = "신고자 정보 없음";
//        String reporterEmail = null; // Customer 엔티티에 getEmail()이 있다고 가정
//
//        if (report.getReporterId() != null) {
//            reporterIdAsLong = report.getReporterId().longValue();
//            Optional<Customer> customerOpt = customerRepository.findById(reporterIdAsLong);
//            if (customerOpt.isPresent()) {
//                Customer reporter = customerOpt.get();
//                reporterName = reporter.getName();
//                reporterEmail = reporter.getEmail(); // Customer 엔티티에 getEmail()이 있다고 가정
//            }
//        }
//
//        return AdminReviewReportDetailDTO.builder()
//                .reportId(report.getId())
//                .status(report.getStatus())
//                .reason(report.getReason())
//                .reportedAt(report.getCreatedAt())
//                .reportUpdatedAt(report.getUpdatedAt())
//                .reporterId(reporterIdAsLong)
//                .reporterName(reporterName)
//                .reporterEmail(reporterEmail)
//                .reportedReviewDetail(reportedReviewDetailContent) // AdminReviewReportDetailDTO의 필드명과 일치해야 함
//                .build();
//    }
//}
