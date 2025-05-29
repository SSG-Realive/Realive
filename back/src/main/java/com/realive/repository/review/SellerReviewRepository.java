package com.realive.repository.review;

import com.realive.domain.seller.SellerReview;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <<< 추가: Specification 사용을 위해
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
// JpaSpecificationExecutor<SellerReview>를 상속받도록 추가합니다.
public interface SellerReviewRepository extends JpaRepository<SellerReview, Long>, JpaSpecificationExecutor<SellerReview> {

    // --- 특정 조건으로 SellerReview 엔티티 전체를 조회하는 메소드들 (사용자님 코드 유지) ---

    /**
     * 특정 상품 ID에 해당하는 리뷰 목록을 페이징하여 조회합니다.
     * SellerReview 엔티티에 'order.product.id' 경로로 접근 가능한 Product 참조가 있어야 합니다.
     */
    @Query("SELECT sr FROM SellerReview sr JOIN FETCH sr.order o JOIN FETCH o.product p WHERE p.id = :productId")
    Page<SellerReview> findReviewsByProductId(@Param("productId") Long productId, Pageable pageable);

    /**
     * 특정 고객 ID(작성자)가 작성한 리뷰 목록을 페이징하여 조회합니다.
     * SellerReview 엔티티의 'customer' 필드를 사용합니다.
     */
    @Query("SELECT sr FROM SellerReview sr JOIN FETCH sr.customer c WHERE c.id = :customerId")
    Page<SellerReview> findReviewsByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * 특정 판매자 ID가 받은 리뷰 목록을 페이징하여 조회합니다.
     * SellerReview 엔티티의 'seller' 필드를 사용합니다.
     */
    @Query("SELECT sr FROM SellerReview sr JOIN FETCH sr.seller s WHERE s.id = :sellerId")
    Page<SellerReview> findReviewsBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);


    // --- 통계 계산을 위한 메소드들 (사용자님 코드 주석 유지) ---
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

    // --- 삭제 관련 메소드들 (사용자님 코드 유지) ---
    /**
     * 특정 고객 ID(작성자)에 해당하는 모든 판매자 리뷰를 데이터베이스에서 직접 삭제합니다.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SellerReview sr WHERE sr.customer.id = :customerId")
    int deleteByCustomerId(@Param("customerId") Long customerId);

    /**
     * 특정 판매자 ID가 받은 모든 판매자 리뷰를 데이터베이스에서 직접 삭제합니다.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    int deleteBySellerId(@Param("sellerId") Long sellerId);

    // JpaSpecificationExecutor를 상속받았으므로,
    // 별도의 findAll(Specification<SellerReview> spec, Pageable pageable) 메소드 선언은 필요 없습니다.
    // Spring Data JPA가 자동으로 해당 메소드를 제공합니다.
}
