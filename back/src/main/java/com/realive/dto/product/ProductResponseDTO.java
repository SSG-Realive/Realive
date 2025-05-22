package com.realive.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품 상세 조회 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;              // 상품 ID - Product
    private String name;          // 상품명 - Product
    private String description;   // 설명 - Product
    private int price;            // 가격 - Product
    private int stock;            // 재고 - Product
    private Integer width;         // 가구 크기 (cm) - Product  
    private Integer depth;       // 가구 크기 (cm) - Product
    private Integer height;      // 가구 크기 (cm) - Product
    private String status;        // 상태 (enum → String) - Product
    private boolean isActive;     // 판매 여부 - Product(url)
    private String thumbnailUrl;      // 대표 이미지 URL - ProductImage
    private String categoryName;  // 카테고리 이름 (선택) - Category(name)
    private String sellerName;    // 판매자 이름 (선택) -Seller(name)
}