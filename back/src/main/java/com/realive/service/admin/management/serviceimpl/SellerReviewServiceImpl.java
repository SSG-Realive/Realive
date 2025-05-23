package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.review.SellerReview;
import com.realive.dto.admin.management.ReviewDTO;
import com.realive.repository.review.SellerReviewRepository;
import com.realive.repository.seller.SellerRepository;
import com.realive.service.admin.management.service.SellerReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerReviewServiceImpl implements SellerReviewService {

    private final SellerReviewRepository sellerReviewRepository;
    private final SellerRepository sellerRepository;

    @Override
    public Page<ReviewDTO> getSellerReviews(Integer sellerId, Pageable pageable) {
        return sellerReviewRepository.findReviewsBySellerId(sellerId.longValue(), pageable)
                .map(this::convertToReviewDTO);
    }

    @Override
    public Map<String, Object> getSellerReviewStatistics(Integer sellerId) {
        if (!sellerRepository.existsById(sellerId.longValue())) {
            throw new NoSuchElementException("판매자 없음 ID: " + sellerId);
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("sellerId", sellerId);
        stats.put("averageRating", sellerReviewRepository.getAverageRatingBySellerId(sellerId.longValue()));
        stats.put("totalReviews", sellerReviewRepository.countReviewsBySellerId(sellerId.longValue()));
        return stats;
    }

    private ReviewDTO convertToReviewDTO(SellerReview e) {
        return ReviewDTO.builder().id(e.getId()).productId(e.getProduct() != null ? e.getProduct().getId() : null).productName(e.getProduct() != null ? e.getProduct().getName() : null).customerId(e.getCustomer() != null ? e.getCustomer().getId() : null).customerName(e.getCustomer() != null ? e.getCustomer().getName() : null).rating(e.getRating()).content(e.getContent()).createdAt(e.getCreatedAt()).build();
    }
}
