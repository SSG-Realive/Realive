package com.realive.dto.review;

import lombok.*;
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

}
