package com.realive.service.review.view;

import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.repository.review.view.ReviewDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReviewViewServiceImpl implements ReviewViewService {

    private final ReviewDetail reviewDetail;

    //판매자에 대한 리뷰 리스트 조회
    @Override
    public Page<ReviewListResponseDTO> getReviewList(Long sellerId, Pageable pageable) {
        log.info("Fetching review list for sellerId: {}, page: {}", sellerId, pageable.getPageNumber());
        if (sellerId == null || sellerId <= 0) {
            log.error("Invalid seller ID: {}", sellerId);
            throw new IllegalArgumentException("Invalid seller ID");
        }
        Page<ReviewListResponseDTO> reviews = reviewDetail.findSellerReviewsBySellerId(sellerId, pageable);
        log.info("Retrieved {} reviews for sellerId: {}", reviews.getTotalElements(), sellerId);
        return reviews;
    }

    //리뷰 상세 조회
    @Override
    public ReviewResponseDTO getReviewDetail(Long id) {
        log.info("Fetching review detail for id: {}", id);
        ReviewResponseDTO review = reviewDetail.findReviewDetailById(id)
                .orElseThrow(() -> {
                    log.error("Review not found with id: {}", id);
                    return new IllegalArgumentException("Review not found with id: " + id);
                });
        log.info("Retrieved review detail for id: {}", id);
        return review;
    }

    //내 리뷰리스트 조회
    @Override
    public Page<ReviewListResponseDTO> getMyReviewList(Long customerId, Pageable pageable) {
        log.info("Fetching review list for customerId: {}, page: {}", customerId, pageable.getPageNumber());
        if (customerId == null || customerId <= 0) {
            log.error("Invalid customer ID: {}", customerId);
            throw new IllegalArgumentException("Invalid customer ID");
        }
        Page<ReviewListResponseDTO> reviews = reviewDetail.findSellerReviewsByMe(customerId, pageable);
        log.info("Retrieved {} reviews for customerId: {}", reviews.getTotalElements(), customerId);
        return reviews;
    }
}