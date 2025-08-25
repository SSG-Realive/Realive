package com.realive.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 추천용 상품 요약 정보 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeaturedProductSummaryResponseDTO {
    private Long productId;           // 상품 ID
    private String name;              // 상품명
    private int price;                // 가격
    private String imageThumbnailUrl; // 대표 이미지 썸네일
}
