package com.realive.service.review.crud;

import com.realive.dto.review.ReviewCreateRequestDTO;
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.dto.review.ReviewUpdateRequestDTO;

public interface ReviewCRUDService {
    ReviewResponseDTO createReview(ReviewCreateRequestDTO requestDTO, Long customerId);
    ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateRequestDTO requestDTO, Long customerId);
    void deleteReview(Long reviewId, Long customerId);

    boolean checkReviewExistence(Long orderId, Long customerId);
}