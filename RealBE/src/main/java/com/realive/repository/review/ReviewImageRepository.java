//package com.realive.repository.review;
//
//import com.realive.domain.review.SellerReviewImage;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface ReviewImageRepository extends JpaRepository<SellerReviewImage, Long> {
//
//    // 리뷰 ID로 이미지 전체 조회
//    List<SellerReviewImage> findByReviewId(Long reviewId);
//
//    // 썸네일만 조회 (필드명에 맞게 수정)
//    List<SellerReviewImage> findByReviewIdAndThumbnailTrue(Long reviewId);
//
//    // 삭제용
//    void deleteByReviewId(Long reviewId);
//}
