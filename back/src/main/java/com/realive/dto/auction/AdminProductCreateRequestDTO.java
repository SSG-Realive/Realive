package com.realive.dto.auction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

// 상품 등록 요청
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProductCreateRequestDTO {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Integer productId;

    @NotNull(message = "구매 가격은 필수입니다.")
    @Positive(message = "구매 가격은 0보다 커야 합니다.")
    private Integer purchasePrice;

    @NotNull(message = "판매자 ID는 필수입니다.")
    private Integer purchasedFromSellerId;

    public com.realive.domain.auction.AdminProduct toEntity() {
        return com.realive.domain.auction.AdminProduct.builder()
                .productId(this.productId)
                .purchasePrice(this.purchasePrice)
                .purchasedFromSellerId(this.purchasedFromSellerId)
                .purchasedAt(java.time.LocalDateTime.now())
                .isAuctioned(false)
                .build();
    }
}
