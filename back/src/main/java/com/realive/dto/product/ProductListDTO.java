package com.realive.dto.product;

import com.realive.domain.product.Product;

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
public class ProductListDTO {

    private Long id;                   // 상품 ID
    private String name;               // 상품명
    private int price;                 // 가격
    private String status;             // 상태 (상, 중, 하)
    private boolean isActive;          // 판매 여부
    private String imageThumbnailUrl;  // 대표 이미지 썸네일
    

    public static ProductListDTO from(Product product, String imageUrl) {
        return ProductListDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .status(product.getStatus().name())  // enum 처리
                .isActive(product.isActive())
                .imageThumbnailUrl(imageUrl)
                .build();
    }
}