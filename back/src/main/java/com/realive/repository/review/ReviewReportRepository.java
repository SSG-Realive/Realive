package com.realive.repository.review;

import com.realive.domain.review.ReviewReport;
import com.realive.domain.common.enums.ReviewReportStatus; // Enum 경로
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // 선택적 (서비스 계층에서 관리 시)
import java.util.List;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Integer> { // ID 타입 Integer

    /**
     * 특정 신고자 ID(고객 ID)에 해당하는 모든 리뷰 신고 내역을 삭제합니다.
     * @param reporterId 신고자(고객) ID (Customer.id가 Long이므로 타입 불일치 주의 -> 서비스에서 형변환 필요)
     * @return 삭제된 신고 내역의 수
     */
    @Modifying
    @Transactional // Repository 레벨 트랜잭션 (서비스에서 이미 관리하면 생략 가능)
    @Query("DELETE FROM ReviewReport rr WHERE rr.reporterId = :reporterId")
    int deleteByReporterId(@Param("reporterId") Integer reporterId);

    /**
     * 특정 판매자 리뷰 ID들에 해당하는 모든 리뷰 신고 내역을 삭제합니다.
     * (예: 특정 판매자가 받은 리뷰들이 삭제될 때, 그 리뷰들에 대한 신고들도 함께 삭제)
     * @param sellerReviewIds 판매자 리뷰 ID 목록 (SellerReview.id가 Long이므로 타입 불일치 주의 -> 서비스에서 형변환 필요)
     * @return 삭제된 신고 내역의 수
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ReviewReport rr WHERE rr.sellerReviewId IN :sellerReviewIds")
    int deleteAllBySellerReviewIdIn(@Param("sellerReviewIds") List<Integer> sellerReviewIds);

    // 필요에 따라 추가적인 조회/삭제 메소드들
    // 예: 특정 판매자 리뷰 ID에 대한 모든 신고 조회
    List<ReviewReport> findAllBySellerReviewId(Integer sellerReviewId);

    /**
     * 특정 처리 상태(status)를 가진 모든 리뷰 신고 내역을 페이징하여 조회합니다.
     * @param status 조회할 신고 처리 상태
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 등)
     * @return 해당 상태의 신고 목록 페이지
     */
    Page<ReviewReport> findAllByStatus(ReviewReportStatus status, Pageable pageable);
}
