package com.realive.dto.product;

import com.realive.domain.product.Category;
import com.realive.domain.product.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String imageThumbnailUrl;  // 대표 이미지 썸네일A
    private Long categoryId;           // 카테고리 ID
    private String parentCategoryName;
    private String categoryName;
    private String sellerName;
    private Long sellerId;             // 판매자 ID
    private int stock;                 // 재고
    private LocalDateTime createdAt;   // 등록일

    public static ProductListDTO from(Product product, String imageUrl) {
        return ProductListDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .status(product.getStatus().name())  // enum 처리
                .isActive(product.isActive())
                .imageThumbnailUrl(imageUrl)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(Category.getCategoryFullPath(product.getCategory()))
                .parentCategoryName(
                        product.getCategory() != null && product.getCategory().getParent() != null
                                ? product.getCategory().getParent().getName()
                                : null)
                .sellerName(product.getSeller().getName())
                .sellerId(product.getSeller().getId())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .build();
    }
}