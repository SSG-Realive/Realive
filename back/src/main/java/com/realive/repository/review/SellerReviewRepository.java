package com.realive.repository.review;

import com.realive.domain.review.SellerReview; // SellerReview 엔티티 경로
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map; // 평점 분포도 등에 사용될 수 있음

@Repository
public interface SellerReviewRepository extends JpaRepository<SellerReview, Long>, JpaSpecificationExecutor<SellerReview> {

    // --- 특정 조건으로 SellerReview 엔티티 전체를 조회하는 메소드들 ---

    /**
     * 특정 상품 ID에 해당하는 리뷰 목록을 페이징하여 조회합니다.
     * SellerReview 엔티티에 'product.id' 경로로 접근 가능한 Product 참조가 있어야 합니다.
     */
    @Query("SELECT sr FROM SellerReview sr WHERE sr.product.id = :productId")
    Page<SellerReview> findReviewsByProductId(@Param("productId") Long productId, Pageable pageable);

    /**
     * 특정 고객 ID가 작성한 리뷰 목록을 페이징하여 조회합니다.
     * SellerReview 엔티티에 'customer.id' 경로로 접근 가능한 Customer 참조가 있어야 합니다.
     */
    @Query("SELECT sr FROM SellerReview sr WHERE sr.customer.id = :customerId")
    Page<SellerReview> findReviewsByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * 특정 판매자 ID가 판매하는 상품들에 대한 리뷰 목록을 페이징하여 조회합니다.
     * SellerReview 엔티티에 'seller.id' 경로로 접근 가능한 Seller 참조가 있어야 합니다.
     */
    @Query("SELECT sr FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    Page<SellerReview> findReviewsBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);


    // --- 통계 계산을 위한 메소드들 ---

    /**
     * 특정 상품 ID에 대한 평균 평점을 조회합니다.
     */
    @Query("SELECT AVG(sr.rating) FROM SellerReview sr WHERE sr.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * 특정 상품 ID에 대한 총 리뷰 개수를 조회합니다.
     */
    @Query("SELECT COUNT(sr.id) FROM SellerReview sr WHERE sr.product.id = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);

    /**
     * 특정 판매자 ID의 상품들에 대한 평균 평점을 조회합니다.
     */
    @Query("SELECT AVG(sr.rating) FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    Double getAverageRatingBySellerId(@Param("sellerId") Long sellerId);

    /**
     * 특정 판매자 ID의 상품들에 대한 총 리뷰 개수를 조회합니다.
     */
    @Query("SELECT COUNT(sr.id) FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    Long countReviewsBySellerId(@Param("sellerId") Long sellerId);


    // --- 특정 필드만 선택하여 조회하는 예시 (Object[] 또는 DTO 프로젝션 사용 가능) ---

    /**
     * 특정 상품의 리뷰 ID, 평점, 내용을 최신순으로 페이징하여 조회합니다.
     * @param productId 상품 ID
     * @param pageable 페이징 정보 (Sort 포함)
     * @return Object 배열 리스트 페이지 (각 배열은 [id, rating, content])
     */
    @Query("SELECT sr.id, sr.rating, sr.content FROM SellerReview sr WHERE sr.product.id = :productId ORDER BY sr.createdAt DESC")
    Page<Object[]> findReviewIdRatingContentByProductId(@Param("productId") Long productId, Pageable pageable);

    /**
     * 특정 판매자의 모든 리뷰에 대해 상품명, 리뷰 평점, 리뷰 내용을 조회 (판매자 ID, 상품명 순으로 정렬)
     * SellerReview가 Product를, Product가 Seller를 참조해야 함.
     * @param sellerId 판매자 ID
     * @param pageable 페이징 정보
     * @return Object 배열 리스트 페이지 (각 배열은 [productName, rating, content])
     */
    // SellerReview 엔티티에 product.name, product.seller.id 경로 접근 가능해야 함
    @Query("SELECT sr.product.name, sr.rating, sr.content FROM SellerReview sr WHERE sr.seller.id = :sellerId ORDER BY sr.product.name ASC, sr.createdAt DESC")
    Page<Object[]> findProductNameRatingContentBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    // JpaSpecificationExecutor를 상속받았으므로, 복잡한 동적 검색은 Specification API를 통해 처리 가능.
}
