package com.realive.repository.review;

import com.realive.dto.review.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewDetail {
    Optional<ReviewResponseDTO> findReviewDetailById(Long id);
    Page<ReviewResponseDTO> findSellerReviewsBySellerId(Long sellerId, Pageable pageable); // 추가
}
