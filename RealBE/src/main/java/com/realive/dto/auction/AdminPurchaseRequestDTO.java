package com.realive.dto.auction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

// 관리자 상품 매입 요청
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPurchaseRequestDTO {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Integer productId;

    @NotNull(message = "구매 가격은 필수입니다.")
    @Positive(message = "구매 가격은 0보다 커야 합니다.")
    private Integer purchasePrice;

    // DTO -> Entity
    public com.realive.domain.auction.AdminProduct toEntity() {
        return com.realive.domain.auction.AdminProduct.builder()
                .productId(this.productId)
                .purchasePrice(this.purchasePrice)
                .purchasedAt(LocalDateTime.now())
                .isAuctioned(false)
                .build();
    }
}
