package com.realive.dto.review;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long reviewId;
    private Long orderId;
    private String productName;
    private double rating;
    private String comment;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private boolean hidden;
}