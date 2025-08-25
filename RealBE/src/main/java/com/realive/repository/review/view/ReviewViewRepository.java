package com.realive.repository.review.view;

import com.realive.domain.review.SellerReview;
import com.realive.dto.product.ProductSummaryDTO;
import com.realive.dto.review.MyReviewResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface    ReviewViewRepository extends JpaRepository<SellerReview, Long> {

    // 특정 리뷰 상세 정보 조회
    // productName 필드를 DTO 생성자에서 제거하고, 서비스 계층에서 처리하도록 변경
    @Query(value = "SELECT new com.realive.dto.review.ReviewResponseDTO(" +
            "sr.id, sr.order.id, sr.customer.id, sr.seller.id, " +
            "sr.rating, sr.content, sr.createdAt, sr.isHidden) " + // productName 제거
            "FROM SellerReview sr " +
            "WHERE sr.id = :reviewId")
    Optional<ReviewResponseDTO> findReviewDetailById(@Param("reviewId") Long reviewId);

    // 판매자에 대한 리뷰 목록을 페이지네이션으로 조회 (ReviewResponseDTO)
    // productName 필드를 DTO 생성자에서 제거하고, 서비스 계층에서 처리하도록 변경
    @Query(value = "SELECT new com.realive.dto.review.ReviewResponseDTO(" +
            "sr.id, sr.order.id, sr.customer.id, sr.seller.id, " +
            "sr.rating, sr.content, sr.createdAt, sr.isHidden) " + // productName 제거
            "FROM SellerReview sr " +
            "WHERE sr.seller.id = :sellerId " +
            "ORDER BY sr.createdAt DESC",
            countQuery = "SELECT count(sr.id) FROM SellerReview sr WHERE sr.seller.id = :sellerId")
    Page<ReviewResponseDTO> findSellerReviewsBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    // 고객이 작성한 리뷰 목록을 페이지네이션으로 조회 (MyReviewResponseDTO)
    // productName 필드를 DTO 생성자에서 제거하고, 서비스 계층에서 처리하도록 변경
    @Query(value = "SELECT new com.realive.dto.review.MyReviewResponseDTO(" +
            "sr.id, sr.order.id, " +
            "sr.seller.id, sr.rating, sr.content, sr.createdAt) " + // productName 제거
            "FROM SellerReview sr " +
            "WHERE sr.customer.id = :customerId " +
            "ORDER BY sr.createdAt DESC",
            countQuery = "SELECT count(sr.id) FROM SellerReview sr WHERE sr.customer.id = :customerId")
    Page<MyReviewResponseDTO> findMyReviewsByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    // 리뷰 ID 리스트에 해당하는 모든 이미지 URL 조회 (별도 메서드, Q-Class 미사용)
    @Query("SELECT sri.review.id, sri.imageUrl FROM SellerReviewImage sri WHERE sri.review.id IN :reviewIds")
    List<Object[]> findImageUrlsByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    // 추가: 주문 ID 리스트에 해당하는 상품 이름 조회 (서비스 계층에서 사용)
    // OrderItem 엔티티가 Order와 Product를 가지고 있다고 가정합니다.
    @Query("SELECT oi.order.id, p.name FROM OrderItem oi JOIN oi.product p WHERE oi.order.id IN :orderIds GROUP BY oi.order.id, p.name")
    List<Object[]> findProductNamesByOrderIds(@Param("orderIds") List<Long> orderIds);

    @Query("""
        SELECT oi.order.id, p.name
        FROM OrderItem oi
        JOIN oi.product p
        WHERE oi.order.id IN :orderIds
          AND p.seller.id = :sellerId
    """)
    List<Object[]> findProductNamesByOrderIdsAndSellerId(@Param("orderIds") List<Long> orderIds,
                                                         @Param("sellerId") Long sellerId);

    @Query("""
    SELECT new com.realive.dto.product.ProductSummaryDTO(
        p.id, p.name, img.url
    )
    FROM OrderItem oi
    JOIN oi.product p
    LEFT JOIN ProductImage img ON img.product = p AND img.isThumbnail = true
    WHERE oi.order.id IN :orderIds AND p.seller.id = :sellerId
""")
    List<ProductSummaryDTO> findProductSummaryByOrderIdsAndSellerId(@Param("orderIds") List<Long> orderIds,
                                                                    @Param("sellerId") Long sellerId);

    @Query("""
        SELECT sr.seller.id
        FROM SellerReview sr
        WHERE sr.order.id = :orderId
          AND sr.customer.id = :customerId
    """)
        List<Long> findReviewedSellerIds(@Param("orderId") Long orderId, @Param("customerId") Long customerId);


    // 페이징 없이 전체 리뷰 조회하는 메서드 추가
    List<ReviewResponseDTO> findAllSellerReviewsBySellerId(Long sellerId);

    @Query(value = "SELECT new com.realive.dto.review.ReviewResponseDTO(" +
            "sr.id, sr.order.id, sr.customer.id, sr.seller.id, " +
            "sr.rating, sr.content, sr.createdAt, sr.isHidden) " +
            "FROM SellerReview sr " +
            "JOIN sr.order o JOIN o.orderItems oi JOIN oi.product p " +
            "WHERE sr.seller.id = :sellerId " +
            "AND (:productName IS NULL OR p.name LIKE %:productName%) " +
            "ORDER BY sr.createdAt DESC",
            countQuery = "SELECT count(sr.id) FROM SellerReview sr " +
                    "JOIN sr.order o JOIN o.orderItems oi JOIN oi.product p " +
                    "WHERE sr.seller.id = :sellerId " +
                    "AND (:productName IS NULL OR p.name LIKE %:productName%)")
    Page<ReviewResponseDTO> findSellerReviewsBySellerIdAndProductName(
            @Param("sellerId") Long sellerId,
            @Param("productName") String productName,
            Pageable pageable
    );
}