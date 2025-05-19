package com.realive.dto.auction;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

// 경매 등록 요청
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionCreateRequestDTO {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Integer productId;

    @NotNull(message = "시작 가격은 필수입니다.")
    @Positive(message = "시작 가격은 0보다 커야 합니다.")
    private Integer startPrice;

    @NotNull(message = "종료 시간은 필수입니다.")
    @Future(message = "종료 시간은 현재 시간 이후여야 합니다.")
    private LocalDateTime endTime;

    public com.realive.domain.auction.Auction toEntity() {
        LocalDateTime now = LocalDateTime.now();
        return com.realive.domain.auction.Auction.builder()
                .productId(this.productId)
                .startPrice(this.startPrice)
                .currentPrice(this.startPrice)
                .startTime(now)
                .endTime(this.endTime)
                .isClosed(false)
                .build();
    }
}
