package com.realive.dto.auction;

import com.realive.domain.auction.AdminProduct;
import com.realive.domain.product.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// 경매 상품 정보 응답
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProductDTO {
    private Integer id; // AdminProduct의 PK
    private Integer productId; // 원본 Product의 ID
    private String productName; // 원본 Product의 이름
    private String productDescription; // 원본 Product의 설명
    private Integer purchasePrice;
    private Integer purchasedFromSellerId;
    private LocalDateTime purchasedAt;
    private boolean auctioned;
    private String imageThumbnailUrl;
    private List<String> imageUrls;
    private String productStatus; // 상품 상태 (상/중/하)
    private Long productCategoryId; // 상품 카테고리 ID
    private String productCategoryName; // 상품 카테고리명

    public static AdminProductDTO fromEntity(AdminProduct adminProduct, Product product, String imageThumbnailUrl,  List<String> imageUrls) {
        if (adminProduct == null) return null;
        AdminProductDTOBuilder builder = AdminProductDTO.builder()
                .id(adminProduct.getId())
                .productId(adminProduct.getProductId())
                .purchasePrice(adminProduct.getPurchasePrice())
                .purchasedFromSellerId(adminProduct.getPurchasedFromSellerId())
                .purchasedAt(adminProduct.getPurchasedAt())
                .auctioned(adminProduct.isAuctioned())
                .imageThumbnailUrl(imageThumbnailUrl)
                .imageUrls(imageUrls);

        if (product != null) {
            builder.productName(product.getName())
                    .productDescription(product.getDescription())
                    .productStatus(product.getStatus().name());
            
            // 카테고리 정보 추가
            if (product.getCategory() != null) {
                builder.productCategoryId(product.getCategory().getId())
                        .productCategoryName(product.getCategory().getName());
            }
        }
        return builder.build();
    }
}