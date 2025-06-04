package com.realive.repository.review.crud;

import com.realive.domain.review.SellerReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerReviewImageRepository extends JpaRepository<SellerReviewImage, Long> {
    void deleteByReview_Id(Long reviewId);

    // ReviewServiceImpl에서 업데이트 후, 또는 삭제 전 이미지 URL을 가져오기 위함
    List<SellerReviewImage> findByReview_Id(Long reviewId); // SellerReview 엔티티의 ID를 매핑

    // 스케줄러에서 DB에 저장된 모든 이미지 URL을 가져오기 위한 쿼리 (필요시 추가)
    // @Query("SELECT s.imageUrl FROM SellerReviewImage s")
    // List<String> findAllImageUrls();
}