package com.realive.repository.review.view;

import com.realive.dto.review.MyReviewResponseDTO; // ⭐ 추가
import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewDetail {

    //상세 리뷰 보기
    Optional<ReviewResponseDTO> findReviewDetailById(Long id);

    //판매자가 판매한 물품 리뷰 리스트 보기
    Page<ReviewResponseDTO> findSellerReviewsBySellerId(Long sellerId, Pageable pageable); // ⭐ ReviewListResponseDTO -> ReviewResponseDTO로 변경

    //내가 작성한 리뷰 리스트 보기
    Page<MyReviewResponseDTO> findMyReviewsByCustomerId(Long customerId, Pageable pageable); // ⭐ findSellerReviewsByMe -> findMyReviewsByCustomerId, ReviewListResponseDTO -> MyReviewResponseDTO로 변경
}