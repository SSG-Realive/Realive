package com.realive.controller.review;

import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.service.review.view.ReviewViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/review")
@Log4j2
@RequiredArgsConstructor
public class ReviewViewController {

    private final ReviewViewService reviewViewService;

    // 판매자의 판매품 리뷰 리스트 조회
    @GetMapping("/seller/{sellerId}")
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

    //내가 작성한 리뷰조회(security를 고려하지 않아 {customerId}가 url에 포함)
    @GetMapping("/customer/{customerId}/my-reviews")
    public ResponseEntity<Page<ReviewListResponseDTO>> getMyReviews(@PathVariable Long customerId, Pageable pageable) {
        Page<ReviewListResponseDTO> result = reviewViewService.getMyReviewList(customerId, pageable);
        return ResponseEntity.ok(result);
    }
}