//package com.realive.repository.review;
//
//import com.realive.domain.review.SellerReview;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface SellerReviewRepository extends JpaRepository<SellerReview, Long>, JpaSpecificationExecutor<SellerReview> {
//
//    Page<SellerReview> findBySellerId(Long sellerId, Pageable pageable);
//
//    @Query("SELECT AVG(sr.rating) FROM SellerReview sr WHERE sr.seller.id = :sellerId")
//    Double getAverageRatingBySellerId(@Param("sellerId") Long sellerId);
//
//    @Query("SELECT COUNT(sr.id) FROM SellerReview sr WHERE sr.seller.id = :sellerId")
//    Long countReviewsBySellerId(@Param("sellerId") Long sellerId);
//
//    // SellerReview.product 참조가 없으므로 아래 메소드들은 주석 처리하거나 삭제 필요
//    // Page<SellerReview> findByProductId(Long productId, Pageable pageable);
//    // @Query("SELECT AVG(sr.rating) FROM SellerReview sr WHERE sr.product.id = :productId")
//    // Double getAverageRatingByProductId(@Param("productId") Long productId);
//    // @Query("SELECT COUNT(sr.id) FROM SellerReview sr WHERE sr.product.id = :productId")
//    // Long countReviewsByProductId(@Param("productId") Long productId);
//
//    // SellerReview.customer 참조가 없으므로 아래 메소드는 주석 처리하거나 삭제 필요
//    // Page<SellerReview> findByCustomerId(Long customerId, Pageable pageable);
//
//}
