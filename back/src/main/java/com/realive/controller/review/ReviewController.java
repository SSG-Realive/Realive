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
        // customerId를 요청 DTO에서 직접 가져와 사용합니다. (현재 시스템 제약사항 고려)
        ReviewResponseDTO response = reviewCRUDService.createReview(requestDTO, requestDTO.getCustomerId());
        return ResponseEntity.ok(response);
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequestDTO requestDTO
    ) {
        // customerId를 요청 DTO에서 직접 가져와 사용합니다. (현재 시스템 제약사항 고려)
        ReviewResponseDTO response = reviewCRUDService.updateReview(reviewId, requestDTO, requestDTO.getCustomerId());
        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long customerId // customerId를 쿼리 파라미터로 받습니다. (현재 시스템 제약사항 고려)
    ) {
        reviewCRUDService.deleteReview(reviewId, customerId);
        return ResponseEntity.noContent().build();
    }


    // 판매자의 판매품 리뷰 리스트 조회 (경로 변경 및 반환 타입 변경)
    // 기존 /{sellerId} 경로가 상세 조회와 중복되므로, 더 명확한 경로로 변경합니다.
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ReviewListResponseDTO> getReviews(@PathVariable Long sellerId, Pageable pageable) {
        // ReviewViewService에서 반환하는 ReviewListResponseDTO 타입과 일치시킵니다.
        ReviewListResponseDTO result = reviewViewService.getReviewList(sellerId, pageable);
        return ResponseEntity.ok(result);
    }

    // 리뷰 상세 조회 (경로 유지)
    // 이 경로는 단일 리뷰 ID를 통해 상세 정보를 조회합니다.
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getReviewDetail(@PathVariable Long id) {
        ReviewResponseDTO result = reviewViewService.getReviewDetail(id);
        return ResponseEntity.ok(result);
    }

    // 내가 작성한 리뷰 조회 (반환 타입 변경)
    // ReviewViewService에서 Page<MyReviewResponseDTO>를 반환하도록 변경했으므로, 이에 맞춥니다.
    @GetMapping("/my")
    public ResponseEntity<Page<MyReviewResponseDTO>> getMyReviews(@RequestParam Long customerId, Pageable pageable) {
        log.info("Fetching my reviews for customerId: {}", customerId);
        Page<MyReviewResponseDTO> result = reviewViewService.getMyReviewList(customerId, pageable);
        return ResponseEntity.ok(result);
    }
}