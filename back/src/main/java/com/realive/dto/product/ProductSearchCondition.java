package com.realive.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품 검색/필터 조건 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchCondition {

    private String keyword;      // 상품명 키워드
    private Long categoryId;     // 카테고리 ID
    private String status;       // 상품 상태 (상, 중, 하)
    private Integer minPrice;    // 최소 가격
    private Integer maxPrice;    // 최대 가격
    private Boolean isActive;    // 판매 여부 (true/false)
}