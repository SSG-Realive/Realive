package com.realive.dto.review;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class MyReviewResponseDTO {
    private Long reviewId;
    private Long orderId;
    private String productName;
    private double rating;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public MyReviewResponseDTO(Long reviewId, Long orderId,
                               double rating, String content, LocalDateTime createdAt) { // productName 인자 제거
        this.reviewId = reviewId;
        this.orderId = orderId;
        // this.productName = productName; // 여기서 productName 설정 제거
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
    }
}