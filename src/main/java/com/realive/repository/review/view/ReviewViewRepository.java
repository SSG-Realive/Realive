package com.realive.repository.review.view;

import com.realive.domain.review.SellerReview;
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
public interface ReviewViewRepository extends JpaRepository<SellerReview, Long> {

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
            "sr.rating, sr.content, sr.createdAt) " + // productName 제거
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
}