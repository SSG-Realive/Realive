package com.realive.service.admin.management.service;

import com.realive.dto.admin.management.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 리뷰 관리 서비스
 */
public interface ReviewManagementService {
    // 리뷰 목록 조회 (페이징)
    Page<ReviewDTO> getReviews(Pageable pageable);

    // 특정 조건으로 리뷰 검색
    Page<ReviewDTO> searchReviews(Map<String, Object> searchParams, Pageable pageable);

    // 리뷰 상세 정보 조회
    ReviewDTO getReviewById(Integer reviewId);

    // 리뷰 상태 업데이트
    ReviewDTO updateReviewStatus(Integer reviewId, String status);

    // 리뷰 삭제
    void deleteReview(Integer reviewId);

    // 특정 상품의 리뷰 조회
    Page<ReviewDTO> getProductReviews(Integer productId, Pageable pageable);

    // 특정 고객의 리뷰 조회
    Page<ReviewDTO> getCustomerReviews(Integer customerId, Pageable pageable);

    // 특정 판매자의 상품에 대한 리뷰 조회
    Page<ReviewDTO> getSellerProductReviews(Integer sellerId, Pageable pageable);

    // 리뷰 통계 정보 조회
    Map<String, Object> getReviewStatistics(Integer productId);
}