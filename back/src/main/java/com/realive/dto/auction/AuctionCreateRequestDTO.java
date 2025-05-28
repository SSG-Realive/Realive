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
    private Integer productId; // 경매 대상 상품의 ID

    @NotNull(message = "시작 가격은 필수입니다.")
    @Positive(message = "시작 가격은 0보다 커야 합니다.")
    private Integer startPrice; // 경매 시작 가격

    @NotNull(message = "종료 시간은 필수입니다.")
    @Future(message = "종료 시간은 현재 시간 이후여야 합니다.")
    private LocalDateTime endTime; // 경매 종료 시간


    // DTO를 Auction 엔티티로 변환하는 메소드
    public com.realive.domain.auction.Auction toEntity() {
        if (productId == null || startPrice == null || endTime == null) {
            throw new IllegalStateException("필수 필드가 누락되었습니다.");
        }

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
