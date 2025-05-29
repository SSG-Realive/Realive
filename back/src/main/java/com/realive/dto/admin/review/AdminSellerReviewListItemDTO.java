package com.realive.dto.admin.review;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminSellerReviewListItemDTO { //리뷰 목록 조회용
    private Long reviewId;          // SellerReview의 ID
    private String productName;     // SellerReview에 연결된 Product의 이름 (선택적)
    private Long productId;         // SellerReview에 연결된 Product의 ID (선택적)
    private String sellerName;      // SellerReview에 연결된 Seller의 이름
    private Long sellerId;          // SellerReview에 연결된 Seller의 ID
    private String customerName;    // SellerReview를 작성한 Customer의 이름
    private Long customerId;        // SellerReview를 작성한 Customer의 ID
    private int rating;             // 평점
    private String contentSummary;  // 리뷰 내용 요약 (예: 앞 100자)
    private LocalDateTime createdAt;  // 리뷰 작성일
    // private Boolean isHidden;    // SellerReview 엔티티에 숨김/공개 상태 필드가 있다면 추가
}
