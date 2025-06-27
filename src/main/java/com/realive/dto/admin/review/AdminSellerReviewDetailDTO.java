package com.realive.dto.admin.review;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminSellerReviewDetailDTO {
    private Long reviewId;        // SellerReview.id (Long)
    private Long orderId;         // 연관된 Order.id (Long)
    private String productName;   // SellerReview -> Order -> Product 경로로 가져옴
    private Long productId;       // SellerReview -> Order -> Product 경로로 가져옴
    private String sellerName;    // SellerReview -> Seller 경로로 가져옴
    private Long sellerId;        // SellerReview -> Seller 경로로 가져옴
    private String customerName;  // SellerReview -> Customer 경로로 가져옴
    private Long customerId;      // SellerReview -> Customer 경로로 가져옴
    private double rating;        // 평점
    private List<String> imageUrls; // 현재 SellerReview 엔티티 구조상 채울 수 없음 (빈 리스트 또는 null)
    private String content;       // 전체 리뷰 내용
    private LocalDateTime createdAt; // 리뷰 작성일
    private LocalDateTime updatedAt; // 리뷰 수정일
    private Boolean isHidden;     // 현재 SellerReview 엔티ति 구조상 채울 수 없음 (null 또는 기본값)
    private String adminMemo;     // 현재 SellerReview 엔티티 구조상 채울 수 없음 (null 또는 기본값)
}
