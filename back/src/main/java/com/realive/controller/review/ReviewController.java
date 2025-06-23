package com.realive.controller.review;

import com.realive.dto.review.*;
import com.realive.security.customer.CustomerPrincipal;
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

@RestController
@RequestMapping("/api/reviews")
@Log4j2
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCRUDService reviewCRUDService;
    private final ReviewViewService reviewViewService;

    // ✅ 리뷰 생성
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @AuthenticationPrincipal CustomerPrincipal customerPrincipal,
            @Valid @ConvertGroup(to = ReviewCreateRequestDTO.CreateValidation.class) @RequestBody ReviewCreateRequestDTO requestDTO
    ) {
        Long customerId = customerPrincipal.getId();
        ReviewResponseDTO response = reviewCRUDService.createReview(requestDTO, customerId);
        return ResponseEntity.ok(response);
    }

    // ✅ 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @AuthenticationPrincipal CustomerPrincipal customerPrincipal,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequestDTO requestDTO
    ) {
        Long customerId = customerPrincipal.getId();
        ReviewResponseDTO response = reviewCRUDService.updateReview(reviewId, requestDTO, customerId);
        return ResponseEntity.ok(response);
    }

    // ✅ 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal CustomerPrincipal customerPrincipal,
            @PathVariable Long reviewId
    ) {
        Long customerId = customerPrincipal.getId();
        reviewCRUDService.deleteReview(reviewId, customerId);
        return ResponseEntity.noContent().build();
    }

    // ✅ 판매자의 리뷰 리스트 조회
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ReviewListResponseDTO> getReviews(@PathVariable Long sellerId, Pageable pageable) {
        ReviewListResponseDTO result = reviewViewService.getReviewList(sellerId, pageable);
        return ResponseEntity.ok(result);
    }

    // ✅ 리뷰 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getReviewDetail(@PathVariable Long id) {
        ReviewResponseDTO result = reviewViewService.getReviewDetail(id);
        return ResponseEntity.ok(result);
    }

    // ✅ 내가 작성한 리뷰 목록 조회
    @GetMapping("/my")
    public ResponseEntity<Page<MyReviewResponseDTO>> getMyReviews(
            @AuthenticationPrincipal CustomerPrincipal customerPrincipal,
            Pageable pageable
    ) {
        Long customerId = customerPrincipal.getId();
        log.info("Fetching my reviews for customerId: {}", customerId);
        Page<MyReviewResponseDTO> result = reviewViewService.getMyReviewList(customerId, pageable);
        return ResponseEntity.ok(result);
    }
}
