package com.realive.repository.review.crud;

import com.realive.domain.review.SellerReviewImage;
import org.springframework.data.jpa.repository.JpaRepository; // CrudRepository 대신 JpaRepository 사용 권장
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerReviewImageRepository extends JpaRepository<SellerReviewImage, Integer> {

    List<SellerReviewImage> findByReviewId(Long reviewId);

    void deleteByReviewId(Long reviewId);
}