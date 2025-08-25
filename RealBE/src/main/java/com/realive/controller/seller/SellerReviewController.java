package com.realive.controller.seller;

import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.security.seller.SellerPrincipal;
import com.realive.service.review.view.ReviewViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/reviews")
@Log4j2
@RequiredArgsConstructor
public class SellerReviewController {

    private final ReviewViewService reviewViewService;

    /**
     * 현재 로그인한 판매자에 대한 리뷰 목록 조회
     */
    @GetMapping
    public ResponseEntity<ReviewListResponseDTO> getMyReviews(
            @AuthenticationPrincipal SellerPrincipal sellerPrincipal,
            @RequestParam(required = false) String productName,  // ← 추가
            Pageable pageable
    ) {
        try {
            Long sellerId = sellerPrincipal.getId();
            log.info("판매자 ID {}의 리뷰 목록 조회 요청", sellerId);

            ReviewListResponseDTO result = reviewViewService.getReviewList(sellerId, productName, pageable);

            log.info("판매자 ID {}의 리뷰 {}건 조회 완료", sellerId, result.getTotalCount());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("판매자 리뷰 목록 조회 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 현재 로그인한 판매자의 리뷰 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getMyReviewStatistics(
            @AuthenticationPrincipal SellerPrincipal sellerPrincipal
    ) {
        try {
            Long sellerId = sellerPrincipal.getId();
            log.info("판매자 ID {}의 리뷰 통계 조회 요청", sellerId);

            // Unpaged 대신 충분히 큰 페이지 사이즈 사용
            Pageable largePage = PageRequest.of(0, 10000);
            ReviewListResponseDTO allReviews = reviewViewService.getReviewList(sellerId, null, largePage);

            // 간단한 통계 계산
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalReviews", allReviews.getTotalCount());

            if (allReviews.getTotalCount() > 0) {
                double averageRating = allReviews.getReviews().stream()
                        .mapToDouble(review -> review.getRating())
                        .average()
                        .orElse(0.0);
                statistics.put("averageRating", Math.round(averageRating * 100.0) / 100.0);

                // 평점별 분포
                long rating5 = allReviews.getReviews().stream().mapToLong(r -> r.getRating() == 5.0 ? 1 : 0).sum();
                long rating4 = allReviews.getReviews().stream().mapToLong(r -> r.getRating() == 4.0 ? 1 : 0).sum();
                long rating3 = allReviews.getReviews().stream().mapToLong(r -> r.getRating() == 3.0 ? 1 : 0).sum();
                long rating2 = allReviews.getReviews().stream().mapToLong(r -> r.getRating() == 2.0 ? 1 : 0).sum();
                long rating1 = allReviews.getReviews().stream().mapToLong(r -> r.getRating() == 1.0 ? 1 : 0).sum();

                statistics.put("rating5Count", rating5);
                statistics.put("rating4Count", rating4);
                statistics.put("rating3Count", rating3);
                statistics.put("rating2Count", rating2);
                statistics.put("rating1Count", rating1);
            } else {
                statistics.put("averageRating", 0.0);
                statistics.put("rating5Count", 0L);
                statistics.put("rating4Count", 0L);
                statistics.put("rating3Count", 0L);
                statistics.put("rating2Count", 0L);
                statistics.put("rating1Count", 0L);
            }

            log.info("판매자 ID {}의 리뷰 통계 조회 완료", sellerId);
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("판매자 리뷰 통계 조회 실패", e);
            return ResponseEntity.status(500).build();
        }
    }
}