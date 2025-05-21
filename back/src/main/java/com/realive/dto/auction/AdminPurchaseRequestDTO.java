package com.realive.dto.auction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 관리자 상품 매입 요청
@Getter
@Builder
public class AdminPurchaseRequestDTO {

    @NotNull(message = "상품 ID는 필수입니다.")
    private final Integer productId;

    @NotNull(message = "구매 가격은 필수입니다.")
    @Positive(message = "구매 가격은 0보다 커야 합니다.")
    private final Integer purchasePrice;

    @NotNull(message = "판매자 ID는 필수입니다.")
    private final Integer purchasedFromSellerId;

    // DTO -> Entity
    public com.realive.domain.auction.AdminProduct toEntity() {
        return com.realive.domain.auction.AdminProduct.builder()
                .productId(this.productId)
                .purchasePrice(this.purchasePrice)
                .purchasedFromSellerId(this.purchasedFromSellerId)
                .purchasedAt(LocalDateTime.now())
                .isAuctioned(false)
                .build();
    }
}
