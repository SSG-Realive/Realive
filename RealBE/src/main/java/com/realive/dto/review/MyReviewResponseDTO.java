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
    private Long sellerId;
    private String productName;
    private double rating;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public MyReviewResponseDTO(Long reviewId, Long orderId, Long sellerId,
                               double rating, String content, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
    }

}
