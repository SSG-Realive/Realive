package com.realive.service.review.view;

import com.realive.dto.review.MyReviewResponseDTO;
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

    // 판매자에 대한 리뷰 리스트 조회
    @Override
    public ReviewListResponseDTO getReviewList(Long sellerId, Pageable pageable) {
        log.info("Fetching review list for sellerId: {}, page: {}", sellerId, pageable.getPageNumber());
        if (sellerId == null || sellerId <= 0) {
            log.error("Invalid seller ID: {}", sellerId);
            throw new IllegalArgumentException("Invalid seller ID");
        }
        // ReviewDetail에서 Page<ReviewResponseDTO>를 받아옵니다.
        Page<ReviewResponseDTO> reviewsPage = reviewDetail.findSellerReviewsBySellerId(sellerId, pageable);

        log.info("Retrieved {} reviews for sellerId: {}", reviewsPage.getTotalElements(), sellerId);

        // ReviewListResponseDTO로 변환하여 반환
        return ReviewListResponseDTO.builder()
                .reviews(reviewsPage.getContent()) // 페이지의 실제 리뷰 리스트
                .totalCount(reviewsPage.getTotalElements()) // <-- 캐스팅 제거 (totalCount가 long 타입으로 변경됨)
                .page(reviewsPage.getNumber()) // 현재 페이지 번호 (0부터 시작)
                .size(reviewsPage.getSize()) // 페이지당 요소 개수
                .build();
    }

    // 리뷰 상세 조회
    @Override
    public ReviewResponseDTO getReviewDetail(Long id) {
        log.info("Fetching review detail for id: {}", id);
        // ID 유효성 검사 추가
        if (id == null || id <= 0) {
            log.error("Invalid review ID: {}", id);
            throw new IllegalArgumentException("Invalid review ID");
        }
        ReviewResponseDTO review = reviewDetail.findReviewDetailById(id)
                .orElseThrow(() -> {
                    log.error("Review not found with id: {}", id);
                    return new IllegalArgumentException("Review not found with id: " + id);
                });
        log.info("Retrieved review detail for id: {}", id);
        return review;
    }

    // 내 리뷰리스트 조회
    @Override
    public Page<MyReviewResponseDTO> getMyReviewList(Long customerId, Pageable pageable) {
        log.info("Fetching my review list for customerId: {}, page: {}", customerId, pageable.getPageNumber());
        if (customerId == null || customerId <= 0) {
            log.error("Invalid customer ID: {}", customerId);
            throw new IllegalArgumentException("Invalid customer ID");
        }
        // ReviewDetail의 메서드 이름과 반환 타입을 MyReviewResponseDTO에 맞게 변경
        Page<MyReviewResponseDTO> myReviews = reviewDetail.findMyReviewsByCustomerId(customerId, pageable);
        log.info("Retrieved {} reviews for customerId: {}", myReviews.getTotalElements(), customerId);
        return myReviews;
    }
}