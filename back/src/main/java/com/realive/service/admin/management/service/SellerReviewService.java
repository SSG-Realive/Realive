package com.realive.service.admin.management.service;

import com.realive.dto.admin.management.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 판매자 리뷰 관리 서비스
 */
public interface SellerReviewService {
    // 판매자의 상품에 대한 리뷰 조회
    Page<ReviewDTO> getSellerReviews(Integer sellerId, Pageable pageable);

    // 판매자 리뷰 통계 정보 조회
    Map<String, Object> getSellerReviewStatistics(Integer sellerId);
}