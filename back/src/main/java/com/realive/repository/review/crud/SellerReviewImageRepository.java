package com.realive.repository.review.crud;

import com.realive.domain.review.SellerReviewImage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerReviewImageRepository extends CrudRepository<SellerReviewImage, Integer> {
    List<SellerReviewImage> findByReviewId(Long reviewId);
    void deleteByReviewId(Long reviewId);
}