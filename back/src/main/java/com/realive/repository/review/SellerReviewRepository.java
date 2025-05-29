package com.realive.repository.review; // 실제 패키지 경로로 가정

import com.realive.domain.seller.SellerReview; // 수정된 SellerReview 엔티티 경로
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 필요시 추가
import org.springframework.data.jpa.repository.Modifying; // 추가
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // 추가
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // 추가 (선택적이지만 권장)

// import java.util.Map; // 이전 코드에 있었으나 현재는 불필요

@Repository
// JpaSpecificationExecutor는 이전 코드에 있었으나 현재는 불필요하면 제외
public interface SellerReviewRepository extends JpaRepository<SellerReview, Long> /*, JpaSpecificationExecutor<SellerReview> */ {

    // --- 특정 조건으로 SellerReview 엔티티 전체를 조회하는 메소드들 (이전 코드에서 가져옴, 필요시 사용) ---

    /**
     * 특정 상품 ID에 해당하는 리뷰 목록을 페이징하여 조회합니다.
     * SellerReview 엔티티에 'order.product.id' 경로로 접근 가능한 Product 참조가 있어야 합니다. (Order -> Product 관계 필요)
     */
    @Query("SELECT sr FROM SellerReview sr WHERE sr.order.product.id = :productId")
    Page<SellerReview> findReviewsByProductId(@Param("productId") Long productId, Pageable pageable);

    /**
     * 특정 고객 ID(작성자)가 작성한 리뷰 목록을 페이징하여 조회합니다.
     * SellerReview 엔티티의 'customer' 필드를 사용합니다.
     */
    @Query("SELECT sr FROM SellerReview sr WHERE sr.customer.id = :customerId")
    Page<SellerReview> findReviewsByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * 특정 판매자 ID가 받은 리뷰 목록을 페이징하여 조회합니다.
     * SellerReview 엔티티의 'seller' 필드를 사용합니다.
     */
    @Query("SELECT sr FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    Page<SellerReview> findReviewsBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);


    // --- 통계 계산을 위한 메소드들 (이전 코드에서 가져옴, 필요시 사용) ---
    /*
    @Query("SELECT AVG(sr.rating) FROM SellerReview sr WHERE sr.order.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(sr.id) FROM SellerReview sr WHERE sr.order.product.id = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT AVG(sr.rating) FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    Double getAverageRatingBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(sr.id) FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    Long countReviewsBySellerId(@Param("sellerId") Long sellerId);
    */

    // === "고객 삭제 시 연관된 판매자 리뷰 삭제" 기능을 위해 추가하는 메소드 ===
    /**
     * 특정 고객 ID(작성자)에 해당하는 모든 판매자 리뷰를 데이터베이스에서 직접 삭제합니다.
     * @param customerId 고객 ID
     * @return 삭제된 리뷰의 수 (또는 void)
     */
    @Modifying
    @Transactional // Repository 메소드 레벨 트랜잭션 (서비스 계층에서 이미 트랜잭션이 있다면 생략 가능)
    @Query("DELETE FROM SellerReview sr WHERE sr.customer.id = :customerId")
    int deleteByCustomerId(@Param("customerId") Long customerId);


    // === "판매자 삭제 시 해당 판매자가 받은 모든 리뷰 삭제" 기능을 위해 추가 제안하는 메소드 ===
    /**
     * 특정 판매자 ID가 받은 모든 판매자 리뷰를 데이터베이스에서 직접 삭제합니다.
     * @param sellerId 판매자 ID
     * @return 삭제된 리뷰의 수 (또는 void)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    int deleteBySellerId(@Param("sellerId") Long sellerId);

}
