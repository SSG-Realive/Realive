package com.realive.repository.review;

import com.realive.domain.review.ReviewReport;
import com.realive.domain.common.enums.ReviewReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Integer> {

    /**
     * 특정 신고자 ID(고객 ID)에 해당하는 모든 리뷰 신고 내역을 물리적으로 삭제합니다.
     * <strong>주의:</strong> AdminUserServiceImpl의 사용자 비활성화 로직에서는 이 메소드를 사용하지 않고,
     * 대신 findAllByReporterId로 조회 후 상태를 변경하는 방식을 사용합니다.
     * 이 메소드는 다른 특정 상황에서 물리적 삭제가 필요할 경우를 위해 유지될 수 있습니다.
     *
     * @param reporterId 신고자(고객) ID. ReviewReport 엔티티의 reporterId 필드가 Integer 타입이라고 가정합니다.
     * @return 삭제된 신고 내역의 수
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ReviewReport rr WHERE rr.reporterId = :reporterId")
    int deleteByReporterId(@Param("reporterId") Integer reporterId);

    /**
     * 특정 판매자 리뷰 ID들에 해당하는 모든 리뷰 신고 내역을 물리적으로 삭제합니다.
     * <strong>주의:</strong> 이 메소드 역시 일반적인 사용자 비활성화 로직과는 직접적인 관련이 없을 수 있습니다.
     *
     * @param sellerReviewIds 판매자 리뷰 ID 목록. ReviewReport 엔티티의 sellerReviewId 필드가 Integer 타입이라고 가정합니다.
     * @return 삭제된 신고 내역의 수
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ReviewReport rr WHERE rr.sellerReviewId IN :sellerReviewIds")
    int deleteAllBySellerReviewIdIn(@Param("sellerReviewIds") List<Integer> sellerReviewIds);

    /**
     * 특정 판매자 리뷰 ID에 대한 모든 신고 내역을 조회합니다.
     * ReviewReport 엔티티의 sellerReviewId 필드가 Integer 타입이라고 가정합니다.
     *
     * @param sellerReviewId 조회할 판매자 리뷰의 ID
     * @return 해당 판매자 리뷰에 대한 모든 신고 리스트
     */
    List<ReviewReport> findAllBySellerReviewId(Integer sellerReviewId);

    /**
     * 특정 처리 상태(status)를 가진 모든 리뷰 신고 내역을 페이징하여 조회합니다.
     *
     * @param status 조회할 신고 처리 상태
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 등)
     * @return 해당 상태의 신고 목록 페이지
     */
    Page<ReviewReport> findAllByStatus(ReviewReportStatus status, Pageable pageable);

    // === 사용자 비활성화 시 신고 내역 상태 변경을 위해 추가된 조회 메소드 ===
    /**
     * 특정 신고자 ID(고객 ID)에 해당하는 모든 리뷰 신고 내역을 조회합니다.
     * AdminUserServiceImpl의 사용자 비활성화 로직에서, 해당 사용자가 한 신고들의 상태를
     * 'REPORTER_ACCOUNT_INACTIVE'로 변경하기 위해 사용됩니다.
     * ReviewReport 엔티티의 'reporterId' 필드가 Integer 타입이라고 가정합니다.
     *
     * @param reporterId 조회할 신고자의 ID (Integer 타입)
     * @return 해당 신고자의 모든 리뷰 신고 리스트 (신고가 없으면 빈 리스트 반환)
     */
    List<ReviewReport> findAllByReporterId(Integer reporterId);
    // 만약 ReviewReport 엔티티가 Customer 엔티티를 직접 참조하고 (예: private Customer reporter;),
    // Customer의 ID가 Long 타입이라면, 다음과 같이 선언할 수 있습니다:
    // List<ReviewReport> findAllByReporter_Id(Long reporterCustomerId);
    // 현재는 ReviewReport 엔티티에 reporterId라는 Integer 타입 필드가 있다고 가정합니다.
}
