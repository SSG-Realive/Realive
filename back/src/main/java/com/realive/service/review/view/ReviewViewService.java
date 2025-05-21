package com.realive.service.review.view;

import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewViewService {

    Page<ReviewResponseDTO> getMyReviewList(Pageable pageable);

    //판매자별 리뷰 조회
    Page<ReviewListResponseDTO> getReviewList(Long sellerId, Pageable pageable);

    //상세 보기
    ReviewResponseDTO getReviewDetail(Long id);
}
