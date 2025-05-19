package com.realive.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품 목록 조회 DTO (간략 응답용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListDto {

    private Long id;          // 상품 ID
    private String name;      // 상품명
    private int price;        // 가격
    private String status;    // 상태 (상, 중, 하)
    private boolean isActive; // 판매 여부
    private String thumbnailUrl;
}