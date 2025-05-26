package com.realive.service.review.view;

import com.realive.dto.review.MyReviewResponseDTO;
import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewViewService {

    // 판매자별 리뷰 조회
    // ReviewDetailImpl에서 Page<ReviewResponseDTO>를 반환하므로, 서비스 계층에서 ReviewListResponseDTO로 변환
    ReviewListResponseDTO getReviewList(Long sellerId, Pageable pageable);

    // 상세 보기
    ReviewResponseDTO getReviewDetail(Long id);

    // 내가 작성한 리뷰 목록 조회
    // ReviewDetailImpl에서 Page<MyReviewResponseDTO>를 반환하므로, 서비스 계층에서 MyReviewListResponseDTO(필요시) 또는 Page<MyReviewResponseDTO>를 반환
    // 현재는 MyReviewListResponseDTO 같은 별도 DTO 정의가 없으므로 Page<MyReviewResponseDTO>를 반환하도록 변경
    Page<MyReviewResponseDTO> getMyReviewList(Long customerId, Pageable pageable);
}