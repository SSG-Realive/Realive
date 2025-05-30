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
 * 각 메소드는 관리자 페이지에서 필요한 백엔드 로직을 호출하는 역할을 합니다.
 */
public interface AdminReviewService {

    /**
     * 특정 처리 상태의 신고된 리뷰 목록을 페이징하여 조회합니다.
     * 관리자는 이 기능을 통해 예를 들어 "처리 대기 중(PENDING)"인 신고, "처리 완료(RESOLVED_KEPT 또는 RESOLVED_HIDDEN)"된 신고 등
     * 다양한 상태의 신고 내역을 효율적으로 관리할 수 있습니다.
     *
     * @param status   조회하고자 하는 리뷰 신고의 처리 상태입니다. {@link ReviewReportStatus} Enum 타입을 사용합니다.
     * @param pageable 페이징(페이지 번호, 페이지 크기) 및 정렬 정보를 담고 있는 객체입니다.
     * @return 조건에 맞는 신고된 리뷰 목록을 {@link AdminReviewReportListItemDTO} 리스트 형태로 포함하는 {@link Page} 객체를 반환합니다.
     *         Page 객체는 총 항목 수, 총 페이지 수 등의 추가적인 페이징 정보도 제공합니다.
     */
    Page<AdminReviewReportListItemDTO> getReportedReviewsByStatus(ReviewReportStatus status, Pageable pageable);

    /**
     * 특정 리뷰 신고의 상세 정보를 조회합니다.
     * 관리자가 신고 목록에서 특정 항목을 선택했을 때, 해당 신고의 모든 관련 정보(신고 사유 원문, 신고자 정보,
     * 그리고 신고 대상이 된 판매자 리뷰의 상세 내용까지 포함하여)를 한 번에 확인하기 위해 사용됩니다.
     *
     * @param reportId 조회할 리뷰 신고의 고유 ID (데이터베이스의 ReviewReport 테이블 ID, Integer 타입)입니다.
     * @return 해당 신고 ID에 대한 상세 정보를 담은 {@link AdminReviewReportDetailDTO} 객체를 반환합니다.
     *         이 DTO는 신고된 리뷰의 상세 정보(AdminSellerReviewDetailDTO)도 중첩하여 포함할 수 있습니다.
     * @throws EntityNotFoundException 만약 주어진 reportId에 해당하는 신고 내역이 데이터베이스에 존재하지 않을 경우 이 예외를 발생시킵니다.
     */
    AdminReviewReportDetailDTO getReportDetail(Integer reportId) throws EntityNotFoundException;

    /**
     * 특정 판매자 리뷰의 상세 정보를 조회합니다.
     * 이 메소드는 신고 처리와 직접적인 관련 없이, 관리자가 특정 판매자 리뷰 자체의 전체 내용(상품 정보, 판매자 정보, 고객 정보, 리뷰 평점 및 내용 등)을
     * 확인하고 싶을 때 독립적으로 사용될 수 있습니다. 예를 들어, getReportDetail 메소드에서 신고된 리뷰의 ID만 알 때,
     * 그 리뷰의 전체 내용을 보여주기 위해 내부적으로 이 메소드가 호출될 수 있습니다.
     *
     * @param reviewId 조회할 판매자 리뷰의 고유 ID (데이터베이스의 SellerReview 테이블 ID, Long 타입)입니다.
     * @return 해당 리뷰 ID에 대한 상세 정보를 담은 {@link AdminSellerReviewDetailDTO} 객체를 반환합니다.
     * @throws EntityNotFoundException 만약 주어진 reviewId에 해당하는 판매자 리뷰가 데이터베이스에 존재하지 않을 경우 이 예외를 발생시킵니다.
     */
    AdminSellerReviewDetailDTO getSellerReviewDetail(Long reviewId) throws EntityNotFoundException;

    /**
     * 특정 리뷰 신고에 대해 관리자가 조치를 취하고 해당 신고의 처리 상태를 업데이트합니다.
     * 현재 구현된 기능은 신고의 'status' 필드(예: PENDING -> RESOLVED_KEPT)만 변경하는 것으로 축소되었습니다.
     * 향후 관리자 메모 추가, 리뷰 숨김 처리 연동 등의 기능 확장이 가능합니다.
     *
     * @param reportId 조치를 취할 리뷰 신고의 고유 ID (ReviewReport.id, Integer 타입)입니다.
     * @param actionRequest 관리자가 요청한 조치 내용을 담은 DTO ({@link TakeActionOnReportRequestDTO})입니다.
     *                      현재는 새로운 신고 처리 상태(newStatus) 정보만 포함합니다.
     * @throws EntityNotFoundException 만약 주어진 reportId에 해당하는 신고 내역이 데이터베이스에 존재하지 않을 경우 이 예외를 발생시킵니다.
     */
    void processReportAction(Integer reportId, TakeActionOnReportRequestDTO actionRequest) throws EntityNotFoundException;

    /**
     * 필터링 조건을 포함하여 모든 판매자 리뷰 목록을 페이징하여 조회합니다.
     * 관리자가 시스템에 등록된 전체 판매자 리뷰를 다양한 조건으로 검색하거나 정렬하여 볼 수 있도록 합니다.
     * 신고 여부와 관계없이 모든 리뷰를 대상으로 합니다.
     *
     * @param pageable 페이징(페이지 번호, 페이지 크기) 및 정렬 정보를 담고 있는 객체입니다.
     * @param productFilter (선택적) 특정 상품명을 포함하는 리뷰만 필터링하기 위한 문자열입니다. 값이 없으면 무시됩니다.
     * @param customerFilter (선택적) 특정 고객명을 포함하는 리뷰만 필터링하기 위한 문자열입니다. 값이 없으면 무시됩니다.
     * @param sellerFilter (선택적) 특정 판매자명을 포함하는 리뷰만 필터링하기 위한 문자열입니다. 값이 없으면 무시됩니다.
     * @return 조건에 맞는 판매자 리뷰 목록을 {@link AdminSellerReviewListItemDTO} 리스트 형태로 포함하는 {@link Page} 객체를 반환합니다.
     */
    Page<AdminSellerReviewListItemDTO> getAllSellerReviews(Pageable pageable,
                                                           Optional<String> productFilter,
                                                           Optional<String> customerFilter,
                                                           Optional<String> sellerFilter);

    /**
     * 특정 판매자 리뷰를 시스템에서 (현재는 물리적으로) 삭제합니다.
     * 관리자가 부적절하거나 잘못된 리뷰를 데이터베이스에서 완전히 제거할 필요가 있을 때 사용됩니다.
     *
     * @param reviewId 삭제할 판매자 리뷰의 고유 ID (SellerReview.id, Long 타입)입니다.
     * @throws EntityNotFoundException 만약 주어진 reviewId에 해당하는 판매자 리뷰가 데이터베이스에 존재하지 않을 경우 (삭제 시도 전에 확인).
     */
    void deleteSellerReview(Long reviewId) throws EntityNotFoundException; // "리뷰 삭제 API"를 위한 메소드

    // --- 아래 메소드들은 아직 구현하지 않으므로 인터페이스에서 제외하거나 주석 처리합니다. ---
    // /**
    //  * (예시) 특정 판매자 리뷰의 공개/숨김 상태를 변경합니다.
    //  * 관리자가 부적절하다고 판단되는 리뷰를 사용자들에게 보이지 않도록 숨김 처리하거나, 다시 공개할 때 사용합니다.
    //  * 이 기능을 사용하려면 SellerReview 엔티티에 'isHidden'과 같은 상태 필드가 필요합니다.
    //  *
    //  * @param reviewId 상태를 변경할 판매자 리뷰의 고유 ID (SellerReview.id, Long 타입).
    //  * @param isHidden 리뷰를 숨길지 여부 (true: 숨김, false: 공개).
    //  * @throws EntityNotFoundException 해당 ID의 리뷰가 없을 경우.
    //  */
    // void updateSellerReviewVisibility(Long reviewId, Boolean isHidden) throws EntityNotFoundException;
}
