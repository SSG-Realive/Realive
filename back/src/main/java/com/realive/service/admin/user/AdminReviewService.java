package com.realive.service.admin.user;

import com.realive.domain.common.enums.ReviewReportStatus;
import com.realive.dto.admin.review.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional; // getAllSellerReviews 메소드의 필터 파라미터에 사용

/**
 * 관리자 기능을 위한 리뷰 및 신고 관리 서비스 인터페이스입니다.
 * 이 인터페이스는 관리자가 리뷰 신고를 조회하고, 특정 신고 및 리뷰의 상세 정보를 확인하며,
 * 신고에 대한 조치를 취하고, 전체 리뷰를 관리하는 등의 기능을 수행하기 위한 메소드들을 정의합니다.
 */
public interface AdminReviewService {

    /**
     * 특정 처리 상태의 신고된 리뷰 목록을 페이징하여 조회합니다.
     * @param status 조회하고자 하는 리뷰 신고의 처리 상태 (ReviewReportStatus Enum).
     * @param pageable 페이징 및 정렬 정보.
     * @return 조건에 맞는 신고된 리뷰 목록 (AdminReviewReportListItemDTO 리스트)을 Page 객체로 반환합니다.
     */
    Page<AdminReviewReportListItemDTO> getReportedReviewsByStatus(ReviewReportStatus status, Pageable pageable);

    /**
     * 특정 리뷰 신고의 상세 정보를 조회합니다.
     * @param reportId 조회할 리뷰 신고의 고유 ID (ReviewReport.id, Integer 타입).
     * @return 해당 신고 ID에 대한 상세 정보를 담은 AdminReviewReportDetailDTO 객체를 반환합니다.
     * @throws EntityNotFoundException 해당 ID의 신고가 없을 경우.
     */
    AdminReviewReportDetailDTO getReportDetail(Integer reportId) throws EntityNotFoundException;

    /**
     * 특정 판매자 리뷰의 상세 정보를 조회합니다.
     * @param reviewId 조회할 판매자 리뷰의 고유 ID (SellerReview.id, Long 타입).
     * @return 해당 리뷰 ID에 대한 상세 정보를 담은 AdminSellerReviewDetailDTO 객체를 반환합니다.
     * @throws EntityNotFoundException 해당 ID의 판매자 리뷰가 없을 경우.
     */
    AdminSellerReviewDetailDTO getSellerReviewDetail(Long reviewId) throws EntityNotFoundException;

    /**
     * 특정 리뷰 신고에 대해 관리자가 조치를 취하고 해당 신고의 처리 상태를 업데이트합니다.
     * (현재는 신고의 'status' 필드만 변경)
     * @param reportId 조치를 취할 리뷰 신고의 고유 ID (ReviewReport.id, Integer 타입).
     * @param actionRequest 관리자가 요청한 조치 내용 (newStatus 필드 포함).
     * @throws EntityNotFoundException 해당 ID의 신고가 없을 경우.
     */
    void processReportAction(Integer reportId, TakeActionOnReportRequestDTO actionRequest) throws EntityNotFoundException;

    /**
     * 필터링 조건을 포함하여 모든 판매자 리뷰 목록을 페이징하여 조회합니다.
     * @param pageable 페이징 및 정렬 정보.
     * @param productFilter (선택적) 상품명으로 필터링할 문자열.
     * @param customerFilter (선택적) 고객명으로 필터링할 문자열.
     * @param sellerFilter (선택적) 판매자명으로 필터링할 문자열.
     * @return 조건에 맞는 판매자 리뷰 목록 (AdminSellerReviewListItemDTO 리스트)을 Page 객체로 반환합니다.
     */
    Page<AdminSellerReviewListItemDTO> getAllSellerReviews(Pageable pageable,
                                                           Optional<String> productFilter,
                                                           Optional<String> customerFilter,
                                                           Optional<String> sellerFilter);

    // --- 아래 메소드들은 아직 구현하지 않으므로 인터페이스에서 제외하거나 주석 처리합니다. ---
    // void updateSellerReviewVisibility(Long reviewId, Boolean isHidden) throws EntityNotFoundException;
    // void deleteSellerReview(Long reviewId) throws EntityNotFoundException;
}
