package com.realive.service.admin.management.serviceimpl;


import com.realive.dto.admin.management.ReviewDTO;
import com.realive.repository.ReviewRepository;
import com.realive.service.admin.management.revman.ReviewManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewManagementServiceImpl implements ReviewManagementService {

    private final ReviewRepository reviewRepository;

    @Override
    public Page<ReviewDTO> getReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<ReviewDTO> searchReviews(Map<String, Object> searchParams, Pageable pageable) {
        Specification<Review> spec = Specification.where(null);

        if (searchParams.containsKey("status")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), searchParams.get("status")));
        }

        if (searchParams.containsKey("productId")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("product").get("id"), searchParams.get("productId")));
        }

        if (searchParams.containsKey("customerId")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("customer").get("id"), searchParams.get("customerId")));
        }

        if (searchParams.containsKey("minRating") && searchParams.containsKey("maxRating")) {
            Integer minRating = (Integer) searchParams.get("minRating");
            Integer maxRating = (Integer) searchParams.get("maxRating");
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("rating"), minRating, maxRating));
        }

        if (searchParams.containsKey("content")) {
            String content = (String) searchParams.get("content");
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("content"), "%" + content + "%"));
        }

        return reviewRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public ReviewDTO getReviewById(Integer reviewId) {
        return reviewRepository.findById(reviewId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new NoSuchElementException("리뷰 ID가 존재하지 않습니다: " + reviewId));
    }

    @Override
    @Transactional
    public ReviewDTO updateReviewStatus(Integer reviewId, String status) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("리뷰 ID가 존재하지 않습니다: " + reviewId));

        review.setStatus(status);
        return convertToDTO(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(Integer reviewId) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("리뷰 ID가 존재하지 않습니다: " + reviewId));

        reviewRepository.delete(review);
    }

    @Override
    public Page<ReviewDTO> getProductReviews(Integer productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<ReviewDTO> getCustomerReviews(Integer customerId, Pageable pageable) {
        return reviewRepository.findByCustomerId(customerId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<ReviewDTO> getSellerProductReviews(Integer sellerId, Pageable pageable) {
        return reviewRepository.findByProductSellerId(sellerId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Map<String, Object> getReviewStatistics(Integer productId) {
        // 평균 평점
        Double averageRating = reviewRepository.getAverageRatingByProductId(productId);

        // 평점별 분포
        Map<Integer, Long> ratingDistribution = reviewRepository.getRatingDistributionByProductId(productId);

        // 총 리뷰 개수
        Long totalReviews = reviewRepository.countByProductId(productId);

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