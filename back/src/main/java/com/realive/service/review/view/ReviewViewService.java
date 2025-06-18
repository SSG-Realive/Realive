package com.realive.service.review.view;

import com.realive.dto.review.MyReviewResponseDTO;
import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewViewService {

    // 판매자별 리뷰 조회
    ReviewListResponseDTO getReviewList(Long sellerId, Pageable pageable);

    // 상세 보기
    ReviewResponseDTO getReviewDetail(Long id);

    // 내가 작성한 리뷰 목록 조회
    Page<MyReviewResponseDTO> getMyReviewList(Long customerId, Pageable pageable);
}