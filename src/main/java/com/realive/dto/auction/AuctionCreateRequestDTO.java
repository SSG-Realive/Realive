package com.realive.dto.auction;

import com.realive.domain.auction.AdminProduct;
import com.realive.domain.common.enums.AuctionStatus;
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

    @NotNull(message = "관리자 상품 ID는 필수입니다.")
    private Integer adminProductId; // 경매 대상 관리자 상품의 ID

    @NotNull(message = "시작 가격은 필수입니다.")
    @Positive(message = "시작 가격은 0보다 커야 합니다.")
    private Integer startPrice; // 경매 시작 가격

    @NotNull(message = "시작 시간은 필수입니다.")
    @Future(message = "시작 시간은 현재 시간 이후여야 합니다.")
    private LocalDateTime startTime; // 경매 시작 시간

    @NotNull(message = "종료 시간은 필수입니다.")
    @Future(message = "종료 시간은 현재 시간 이후여야 합니다.")
    private LocalDateTime endTime; // 경매 종료 시간

    // DTO를 Auction 엔티티로 변환하는 메소드
    public com.realive.domain.auction.Auction toEntity(AdminProduct adminProduct) {
        if (adminProduct == null || startPrice == null || startTime == null || endTime == null) {
            throw new IllegalStateException("필수 필드가 누락되었습니다.");
        }

        // 시작 시간이 종료 시간보다 늦으면 안됨
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }

        return com.realive.domain.auction.Auction.builder()
                .adminProduct(adminProduct)
                .startPrice(this.startPrice)
                .currentPrice(this.startPrice)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .status(AuctionStatus.PROCEEDING)
                .build();
    }
}
