package com.realive.controller.review;

import com.realive.dto.review.*;
import com.realive.service.review.crud.ReviewCRUDService;
import com.realive.service.review.view.ReviewViewService;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/reviews")
@Log4j2
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCRUDService reviewCRUDService;
    private final ReviewViewService reviewViewService;

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkReviewExistence(
            @RequestParam Long orderId,
            @AuthenticationPrincipal Long customerId // customerId는 @AuthenticationPrincipal로 받음
    ) {
        if (customerId == null) {
            log.warn("checkReviewExistence - 인증되지 않은 사용자 접근 시도: orderId={}", orderId);
            return ResponseEntity.status(401).body(Map.of("hasReview", false, "message", "로그인이 필요합니다."));
        }

        // ReviewCRUDService.checkReviewExistence는 orderId와 customerId만 받음
        boolean hasReview = reviewCRUDService.checkReviewExistence(orderId, customerId);
        log.info("리뷰 존재 여부 확인: orderId={}, customerId={}, hasReview={}", orderId, customerId, hasReview);
        return ResponseEntity.ok(Map.of("hasReview", hasReview));
    }

    // 리뷰 생성
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @ConvertGroup(to = ReviewCreateRequestDTO.CreateValidation.class) @RequestBody ReviewCreateRequestDTO requestDTO,
            @AuthenticationPrincipal Long customerId // customerId는 @AuthenticationPrincipal로 받음
    ) {
        if (customerId == null) {
            log.warn("createReview - 인증되지 않은 사용자 접근 시도: requestDTO={}", requestDTO);
            return ResponseEntity.status(401).build(); // 401 Unauthorized 반환
        }
        ReviewResponseDTO response = reviewCRUDService.createReview(requestDTO, customerId);
        return ResponseEntity.ok(response);
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequestDTO requestDTO,
            @AuthenticationPrincipal Long customerId // customerId는 @AuthenticationPrincipal로 받음
    ) {
        if (customerId == null) {
            log.warn("updateReview - 인증되지 않은 사용자 접근 시도: reviewId={}, requestDTO={}", reviewId, requestDTO);
            return ResponseEntity.status(401).build(); // 401 Unauthorized 반환
        }
        // requestDTO에서 customerId를 사용하지 않고 @AuthenticationPrincipal에서 받은 customerId 사용
        ReviewResponseDTO response = reviewCRUDService.updateReview(reviewId, requestDTO, customerId);
        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Long customerId // customerId는 @AuthenticationPrincipal로 받음
    ) {
        if (customerId == null) {
            log.warn("deleteReview - 인증되지 않은 사용자 접근 시도: reviewId={}", reviewId);
            return ResponseEntity.status(401).build(); // 401 Unauthorized 반환
        }
        // @RequestParam Long customerId 제거
        reviewCRUDService.deleteReview(reviewId, customerId);
        return ResponseEntity.noContent().build();
    }


    // 판매자의 판매품 리뷰 리스트 조회
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ReviewListResponseDTO> getReviews(@PathVariable Long sellerId, Pageable pageable) {
        ReviewListResponseDTO result = reviewViewService.getReviewList(sellerId, pageable);
        return ResponseEntity.ok(result);
    }

    // 리뷰 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getReviewDetail(@PathVariable Long id) {
        ReviewResponseDTO result = reviewViewService.getReviewDetail(id);
        return ResponseEntity.ok(result);
    }

    // 내가 작성한 리뷰 조회
    @GetMapping("/my")
    public ResponseEntity<Page<MyReviewResponseDTO>> getMyReviews(
            @AuthenticationPrincipal Long customerId, // customerId는 @AuthenticationPrincipal로 받음
            Pageable pageable
    ) {
        if (customerId == null) {
            log.warn("getMyReviews - 인증되지 않은 사용자 접근 시도");
            return ResponseEntity.status(401).build(); // 401 Unauthorized 반환
        }
        log.info("Fetching my reviews for customerId: {}", customerId);
        Page<MyReviewResponseDTO> result = reviewViewService.getMyReviewList(customerId, pageable);
        return ResponseEntity.ok(result);
    }
}