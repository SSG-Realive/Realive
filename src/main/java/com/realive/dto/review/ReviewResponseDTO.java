package com.realive.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long reviewId;
    private Long orderId;
    private Long customerId;
    private Long sellerId;
    private String productName;
    private double rating;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private boolean isHidden;

    public ReviewResponseDTO(Long reviewId, Long orderId, Long customerId, Long sellerId,
                             double rating, String content,
                             LocalDateTime createdAt, boolean isHidden) { // productName 인자 제거
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.sellerId = sellerId;
        // this.productName = productName; // 여기서 productName 설정 제거
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
        this.isHidden = isHidden;
    }

}
