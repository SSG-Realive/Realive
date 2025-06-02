package com.realive.repository.review;

import com.realive.domain.seller.SellerReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository

public interface SellerReviewRepository extends JpaRepository<SellerReview, Long>, JpaSpecificationExecutor<SellerReview> {



    /**
     * 특정 상품 ID에 해당하는 리뷰 목록을 페이징하여 조회합니다.
     * SellerReview 엔티티는 Order를 참조하고, OrderItem 엔티티는 Order와 Product를 참조합니다.
     * 이 관계를 활용하여 FROM 절에 SellerReview와 OrderItem을 명시하고,
     * WHERE 절에서 sr.order = oi.order 와 oi.product.id = :productId 조건으로 필터링합니다.
     * 이 쿼리는 SellerReview 엔티티만 반환하며, 연관된 Order, Customer, Seller 등의 정보는
     * JOIN FETCH 구문이 없으면 지연 로딩될 수 있습니다. (N+1 문제 가능성)
     * 서비스 계층에서 DTO 변환 시 필요한 정보를 효율적으로 로딩하는 전략이 필요합니다.
     * (28번째 줄 부근)
     */
    @Query("SELECT sr FROM SellerReview sr, OrderItem oi " +
            "WHERE sr.order = oi.order " + // SellerReview의 order와 OrderItem의 order를 조인
            "AND oi.product.id = :productId")
    Page<SellerReview> findReviewsByProductId(@Param("productId") Long productId, Pageable pageable);
    // 만약 Order 정보까지 함께 가져오고 싶다면 (N+1 문제 일부 완화 시도):
    // @Query("SELECT DISTINCT sr FROM SellerReview sr JOIN FETCH sr.order o, OrderItem oi " +
    //        "WHERE o = oi.order " + // sr.order 대신 o 사용 (o는 이미 FETCH된 SellerReview의 order)
    //        "AND oi.product.id = :productId")
    // DISTINCT는 여러 OrderItem으로 인해 SellerReview가 중복될 수 있는 경우를 대비합니다.

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


    // --- 통계 계산을 위한 메소드들 (경로 수정) ---
    /**
     * 특정 상품 ID에 대한 평균 평점을 계산합니다.
     * (65번째 줄 부근)
     */
    @Query("SELECT AVG(sr.rating) FROM SellerReview sr, OrderItem oi WHERE sr.order = oi.order AND oi.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * 특정 상품 ID에 대한 리뷰 수를 계산합니다.
     * (68번째 줄 부근)
     */
    @Query("SELECT COUNT(sr) FROM SellerReview sr, OrderItem oi WHERE sr.order = oi.order AND oi.product.id = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);


    @Query("SELECT AVG(sr.rating) FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    Double getAverageRatingBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(sr.id) FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    Long countReviewsBySellerId(@Param("sellerId") Long sellerId);


    // --- 삭제 관련 메소드들 (유지) ---
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

    // === 사용자 비활성화 시 모든 관련 리뷰를 가져오기 위한 non-paging 조회 메소드 추가 (유지) ===
    /**
     * 특정 고객 ID(작성자)가 작성한 모든 리뷰 목록을 조회합니다 (페이징 없음).
     * @param customerId 고객 ID
     * @return 해당 고객이 작성한 모든 리뷰 리스트
     */
    List<SellerReview> findAllByCustomerId(Long customerId); // SellerReview 엔티티의 'customer' 필드 기준

    /**
     * 특정 판매자 ID가 받은 모든 리뷰 목록을 조회합니다 (페이징 없음).
     * @param sellerId 판매자 ID
     * @return 해당 판매자가 받은 모든 리뷰 리스트
     */
    List<SellerReview> findAllBySellerId(Long sellerId); // SellerReview 엔티티의 'seller' 필드 기준
}
