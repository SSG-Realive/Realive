package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.review.SellerReview;
import com.realive.dto.admin.management.ReviewDTO;
import com.realive.repository.review.SellerReviewRepository;
import com.realive.repository.seller.SellerRepository; // 판매자 존재 여부 확인용
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
// import java.util.Optional; // findById 사용 시 필요

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerReviewServiceImpl implements SellerReviewService {

    private final SellerReviewRepository sellerReviewRepository;
    private final SellerRepository sellerRepository; // 판매자 존재 여부 확인을 위해 추가

    @Override
    public Page<ReviewDTO> getSellerReviews(Integer sellerId, Pageable pageable) {
        // SellerReviewRepository에 Page<SellerReview> findBySellerId(Long sellerId, Pageable pageable) 메소드가 있다고 가정
        return sellerReviewRepository.findBySellerId(sellerId.longValue(), pageable)
                .map(this::convertToReviewDTO);
    }

    @Override
    public Map<String, Object> getSellerReviewStatistics(Integer sellerId) {
        // 판매자가 존재하는지 먼저 확인
        if (!sellerRepository.existsById(sellerId.longValue())) {
            throw new NoSuchElementException("판매자 없음 ID: " + sellerId + " (리뷰 통계 조회 시)");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("sellerId", sellerId);

        // SellerReviewRepository에 getAverageRatingBySellerId와 countReviewsBySellerId 메소드가 있다고 가정
        Double averageRating = sellerReviewRepository.getAverageRatingBySellerId(sellerId.longValue());
        Long totalReviews = sellerReviewRepository.countReviewsBySellerId(sellerId.longValue());

        stats.put("averageRating", averageRating != null ? averageRating : 0.0);
        stats.put("totalReviews", totalReviews != null ? totalReviews : 0L);
        return stats;
    }

    private ReviewDTO convertToReviewDTO(SellerReview e) {
        if (e == null) return null;
        ReviewDTO.Builder builder = ReviewDTO.builder()
                .id(e.getId() != null ? e.getId().intValue() : null)
                .rating(e.getRating())
                .content(e.getContent())
                .status(e.getStatus()) // SellerReview 엔티티에 status 필드 가정
                .createdAt(e.getCreatedAt()); // SellerReview 엔티티가 BaseTimeEntity 상속 또는 createdAt 필드 가정

        // SellerReview 엔티티에 product, customer 참조가 있다면 DTO에 추가 설정 가능
        // if (e.getProduct() != null) {
        //     builder.productId(e.getProduct().getId() != null ? e.getProduct().getId().intValue() : null);
        //     builder.productName(e.getProduct().getName());
        // }
        // if (e.getCustomer() != null) {
        //     builder.customerId(e.getCustomer().getId() != null ? e.getCustomer().getId().intValue() : null);
        //     builder.customerName(e.getCustomer().getName());
        // }
        if (e.getSeller() != null) {
            // ReviewDTO에 sellerId, sellerName 필드가 있다면 설정
            // builder.sellerId(e.getSeller().getId() != null ? e.getSeller().getId().intValue() : null);
            // builder.sellerName(e.getSeller().getName());
        }
        return builder.build();
    }
}
