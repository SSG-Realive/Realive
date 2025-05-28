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
        log.info("판매자 ID {}에 대한 리뷰 목록을 조회합니다. 페이지 번호: {}", sellerId, pageable.getPageNumber());
        if (sellerId == null || sellerId <= 0) {
            log.error("유효하지 않은 판매자 ID: {}", sellerId);
            throw new IllegalArgumentException("유효하지않은 판매자 ID입니다.");
        }

        Page<ReviewResponseDTO> reviewsPage = reviewDetail.findSellerReviewsBySellerId(sellerId, pageable);

        log.info("판매자 ID {}에 대한 총 {}개의 리뷰를 조회했습니다.", sellerId, reviewsPage.getTotalElements());

        // ReviewListResponseDTO로 변환하여 반환
        return ReviewListResponseDTO.builder()
                .reviews(reviewsPage.getContent()) // 페이지의 실제 리뷰 리스트
                .totalCount(reviewsPage.getTotalElements())
                .page(reviewsPage.getNumber()) // 현재 페이지 번호 (0부터 시작)
                .size(reviewsPage.getSize()) // 페이지당 요소 개수
                .build();
    }

    // 리뷰 상세 조회
    @Override
    public ReviewResponseDTO getReviewDetail(Long id) {
        log.info("리뷰 상세 정보 ID {}를 조회합니다.", id);
        // ID 유효성 검사 추가
        if (id == null || id <= 0) {
            log.error("유효하지 않은 리뷰 ID: {}", id);
            throw new IllegalArgumentException("Invalid review ID");
        }
        ReviewResponseDTO review = reviewDetail.findReviewDetailById(id)
                .orElseThrow(() -> {
                    log.error("ID {}를 가진 리뷰를 찾을 수 없습니다.", id);
                    return new IllegalArgumentException("Review not found with id: " + id);
                });
        log.info("ID {}에 대한 리뷰 상세 정보를 조회했습니다.", id);
        return review;
    }

    // 내 리뷰리스트 조회
    @Override
    public Page<MyReviewResponseDTO> getMyReviewList(Long customerId, Pageable pageable) {
        log.info("고객 ID {}에 대한 내 리뷰 목록을 조회합니다. 페이지 번호: {}", customerId, pageable.getPageNumber());
        if (customerId == null || customerId <= 0) {
            log.error("유효하지 않은 고객 ID: {}", customerId);
            throw new IllegalArgumentException("Invalid customer ID");
        }
        // ReviewDetail의 메서드 이름과 반환 타입을 MyReviewResponseDTO에 맞게 변경
        Page<MyReviewResponseDTO> myReviews = reviewDetail.findMyReviewsByCustomerId(customerId, pageable);
        log.info("고객 ID {}에 대한 총 {}개의 리뷰를 조회했습니다.", customerId, myReviews.getTotalElements());
        return myReviews;
    }
}