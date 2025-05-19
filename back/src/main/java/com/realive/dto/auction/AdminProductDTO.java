package com.realive.dto.auction;

import lombok.*;
import java.time.LocalDateTime;

// 상품 정보 응답
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProductDTO {

    private Integer id;
    private Integer productId;
    private Integer purchasePrice;
    private Integer purchasedFromSellerId;
    private LocalDateTime purchasedAt;
    private boolean isAuctioned;

    public static AdminProductDTO fromEntity(com.realive.domain.auction.AdminProduct entity) {
        return AdminProductDTO.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .purchasePrice(entity.getPurchasePrice())
                .purchasedFromSellerId(entity.getPurchasedFromSellerId())
                .purchasedAt(entity.getPurchasedAt())
                .isAuctioned(entity.isAuctioned())
                .build();
    }
}
