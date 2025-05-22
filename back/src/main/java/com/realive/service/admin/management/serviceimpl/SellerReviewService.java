package com.realive.service.admin.management.serviceimpl;

import com.realive.dto.admin.management.ReviewDTO;
import com.realive.repository.ReviewRepository;
import com.realive.service.admin.management.sellerman.SellerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerReviewServiceImpl implements SellerReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public Page<ReviewDTO> getSellerReviews(Integer sellerId, Pageable pageable) {
        return reviewRepository.findByProductSellerId(sellerId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Map<String, Object> getSellerReviewStatistics(Integer sellerId) {
        // 평균 평점
        Double averageRating = reviewRepository.getAverageRatingByProductSellerId(sellerId);

        // 평점별 분포
        Map<Integer, Long> ratingDistribution = reviewRepository.getRatingDistributionByProductSellerId(sellerId);

        // 총 리뷰 개수
        Long totalReviews = reviewRepository.countByProductSellerId(sellerId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("averageRating", averageRating != null ? averageRating : 0.0);
        statistics.put("ratingDistribution", ratingDistribution);
        statistics.put("totalReviews", totalReviews);

        return statistics;
    }

    // 엔티티 -> DTO 변환 메소드
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setCustomerId(review.getCustomer().getId());
        dto.setCustomerName(review.getCustomer().getName());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setStatus(review.getStatus());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}