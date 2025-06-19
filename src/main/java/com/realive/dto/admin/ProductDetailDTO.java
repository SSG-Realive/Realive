package com.realive.dto.admin;

import com.realive.domain.product.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 상품 상세 조회 DTO (관리자용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailDTO {
    private Long id;               // 상품 ID
    private String name;           // 상품명
    private String description;    // 상품 설명
    private int price;             // 가격
    private int stock;             // 재고
    private String status;         // 상태 (상, 중, 하)
    private boolean isActive;      // 판매 여부
    private String imageThumbnailUrl;
    private String sellerName;
    private String categoryName;
    private String parentCategoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductDetailDTO from(Product product, String imageUrl) {
        return ProductDetailDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus().name())
                .isActive(product.isActive())
                .imageThumbnailUrl(imageUrl)
                .sellerName(product.getSeller().getName())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .parentCategoryName(
                        product.getCategory() != null && product.getCategory().getParent() != null
                                ? product.getCategory().getParent().getName()
                                : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();

    }
}
