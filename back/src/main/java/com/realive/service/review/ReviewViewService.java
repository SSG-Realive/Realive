package com.realive.service.review;

import com.realive.dto.review.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewViewService {

    //판매자별 리뷰 조회
    Page<ReviewResponseDTO> getReviewList(Long sellerId, Pageable pageable);

    ReviewResponseDTO getReviewDetail(Long id);
}
