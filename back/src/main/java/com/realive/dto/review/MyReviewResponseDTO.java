package com.realive.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyReviewResponseDTO {

    private Long reviewId;
    private Long orderId;
    private String productName; // 주문된 상품명 추가
    private int score;
    private String comment;
    private String imageUrl;

}