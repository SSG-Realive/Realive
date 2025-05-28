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
}