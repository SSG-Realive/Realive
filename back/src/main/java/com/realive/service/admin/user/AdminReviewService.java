//// 경로: com/realive/service/admin/user/AdminReviewService.java
//package com.realive.service.admin.user;
//
//import com.realive.domain.common.enums.ReviewReportStatus;
//import com.realive.dto.admin.review.AdminReviewReportDetailDTO;
//import com.realive.dto.admin.review.AdminReviewReportListItemDTO;
//import com.realive.dto.admin.review.AdminSellerReviewDetailDTO;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//public interface AdminReviewService {
//
//    /**
//     * 특정 처리 상태의 신고된 리뷰 목록을 페이징하여 조회합니다.
//     * @param status 조회할 신고 상태
//     * @param pageable 페이징 정보
//     * @return 페이징된 신고 목록 DTO
//     */
//    Page<AdminReviewReportListItemDTO> getReportedReviewsByStatus(ReviewReportStatus status, Pageable pageable);
//
//    /**
//     * 특정 리뷰 신고의 상세 정보를 조회합니다.
//     * @param reportId 신고 ID (ReviewReport.id, Integer 타입)
//     * @return 신고 상세 정보 DTO
//     * @throws EntityNotFoundException 해당 ID의 신고가 없을 경우
//     */
//    AdminReviewReportDetailDTO getReportDetail(Integer reportId) throws EntityNotFoundException;
//
//    /**
//     * 특정 판매자 리뷰의 상세 정보를 조회합니다.
//     * @param reviewId 조회할 판매자 리뷰의 ID (SellerReview.id, Long 타입)
//     * @return 판매자 리뷰 상세 정보 DTO
//     * @throws EntityNotFoundException 해당 ID의 리뷰가 없을 경우
//     */
//    AdminSellerReviewDetailDTO getSellerReviewDetail(Long reviewId) throws EntityNotFoundException;
//
//    // --- 향후 추가될 "리뷰/신고 관리" 기능 메소드들 (예시) ---
//    /*
//    void processReportAction(Integer reportId, TakeActionOnReportRequestDTO actionRequest) throws EntityNotFoundException;
//    Page<AdminSellerReviewListItemDTO> getAllSellerReviews(Pageable pageable, Optional<String> productFilter, Optional<String> customerFilter);
//    void updateSellerReviewVisibility(Long reviewId, Boolean isHidden) throws EntityNotFoundException;
//    void deleteSellerReview(Long reviewId) throws EntityNotFoundException;
//    */
//}
