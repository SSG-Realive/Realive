package com.realive.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 리뷰 상세 하단에 표시할 간단한 상품 요약 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryDTO {

    private Long id;              // 상품 ID
    private String name;          // 상품명
    private String imageThumbnailUrl;      // 대표 이미지 URL

}
