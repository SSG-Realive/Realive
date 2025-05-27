package com.realive.dto.auction;

import com.realive.domain.auction.AdminProduct;
import com.realive.domain.product.Product;
import lombok.*;
import java.time.LocalDateTime;

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
    private boolean auctioned; // AdminProduct 엔티티의 isAuctioned() getter와 매칭


    public static AdminProductDTO fromEntity(AdminProduct adminProduct, Product product) {
        if (adminProduct == null) {
            return null;
        }
        AdminProductDTOBuilder builder = AdminProductDTO.builder()
                .id(adminProduct.getId()) // AdminProduct의 id 필드를 사용
                .productId(adminProduct.getProductId())
                .purchasePrice(adminProduct.getPurchasePrice())
                .purchasedFromSellerId(adminProduct.getPurchasedFromSellerId())
                .purchasedAt(adminProduct.getPurchasedAt())
                .auctioned(adminProduct.isAuctioned());

        if (product != null) {
            // Product 엔티티에 getName(), getDescription() 메소드가 있다고 가정
            builder.productName(product.getName())
                    .productDescription(product.getDescription());
            // 필요하다면 다른 Product 정보도 추가
        }
        return builder.build();
    }

    // 만약 Product 정보 없이 AdminProduct만으로 DTO를 만드는 경우도 있다면,
    // 아래와 같이 오버로딩된 메소드를 유지할 수 있습니다.
    public static AdminProductDTO fromEntity(AdminProduct entity) {
        if (entity == null) {
            return null;
        }
        return AdminProductDTO.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .purchasePrice(entity.getPurchasePrice())
                .purchasedFromSellerId(entity.getPurchasedFromSellerId())
                .purchasedAt(entity.getPurchasedAt())
                .auctioned(entity.isAuctioned())
                .build();
    }
}
