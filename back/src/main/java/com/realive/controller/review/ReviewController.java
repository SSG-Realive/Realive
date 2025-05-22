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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@Log4j2
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCRUDService reviewCRUDService;
    private final ReviewViewService reviewViewService;

    // 리뷰 생성
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @ConvertGroup(to = ReviewCreateRequestDTO.CreateValidation.class) @RequestBody ReviewCreateRequestDTO requestDTO
    ) {
        ReviewResponseDTO response = reviewCRUDService.createReview(requestDTO, requestDTO.getCustomerId());
        return ResponseEntity.ok(response);
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequestDTO requestDTO
    ) {
        ReviewResponseDTO response = reviewCRUDService.updateReview(reviewId, requestDTO, requestDTO.getCustomerId());
        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long customerId
    ) {
        reviewCRUDService.deleteReview(reviewId, customerId);
        return ResponseEntity.noContent().build();
    }


    // 판매자의 판매품 리뷰 리스트 조회
    @GetMapping("/{sellerId}")
    public ResponseEntity<Page<ReviewListResponseDTO>> getReviews(@PathVariable Long sellerId, Pageable pageable) {
        Page<ReviewListResponseDTO> result = reviewViewService.getReviewList(sellerId, pageable);
        return ResponseEntity.ok(result);
    }

    // 리뷰 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getReviewDetail(@PathVariable Long id) {
        ReviewResponseDTO result = reviewViewService.getReviewDetail(id);
        return ResponseEntity.ok(result);
    }

    //내가 작성한 리뷰조회(security를 고려하지 않고 만들어서 customerId가 필요)
    @GetMapping("/my")
    public ResponseEntity<Page<ReviewListResponseDTO>> getMyReviews(@RequestParam Long customerId, Pageable pageable) {
        log.info("Fetching my reviews for customerId: {}", customerId);
        Page<ReviewListResponseDTO> result = reviewViewService.getMyReviewList(customerId, pageable);
        return ResponseEntity.ok(result);
    }
}