package com.realive.service.review.view;

import com.realive.dto.review.MyReviewResponseDTO;
import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.repository.review.view.ReviewDetailImpl; // ReviewDetail 인터페이스 대신 ReviewDetailImpl을 import
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReviewViewServiceImpl implements ReviewViewService {

    private final ReviewDetailImpl reviewDetailImpl; // ReviewDetail 인터페이스 대신 ReviewDetailImpl을 주입받습니다.

    // 판매자에 대한 리뷰 리스트 조회
    @Override
    public ReviewListResponseDTO getReviewList(Long sellerId, Pageable pageable) {
        log.info("판매자 ID {}에 대한 리뷰 목록을 조회합니다. 페이지 번호: {}", sellerId, pageable.getPageNumber());
        if (sellerId == null || sellerId <= 0) {
            log.error("유효하지 않은 판매자 ID: {}", sellerId);
            throw new IllegalArgumentException("유효하지않은 판매자 ID입니다.");
        }

        // reviewDetail.findSellerReviewsBySellerId 대신 reviewDetailImpl.findSellerReviewsBySellerId 호출
        Page<ReviewResponseDTO> reviewsPage = reviewDetailImpl.findSellerReviewsBySellerId(sellerId, pageable);

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
        // reviewDetail.findReviewDetailById 대신 reviewDetailImpl.findReviewDetailById 호출
        ReviewResponseDTO review = reviewDetailImpl.findReviewDetailById(id)
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
        // reviewDetail.findMyReviewsByCustomerId 대신 reviewDetailImpl.findMyReviewsByCustomerId 호출
        Page<MyReviewResponseDTO> myReviews = reviewDetailImpl.findMyReviewsByCustomerId(customerId, pageable);
        log.info("고객 ID {}에 대한 총 {}개의 리뷰를 조회했습니다.", customerId, myReviews.getTotalElements());
        return myReviews;
    }
}