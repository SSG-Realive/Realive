package com.realive.dto.auction;

import jakarta.validation.constraints.NotNull;
import lombok.*;

// 경매 낙찰 처리 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionFinishRequestDTO {
    @NotNull(message = "경매 ID는 필수입니다.")
    private Integer auctionId;

    @NotNull(message = "낙찰자 ID는 필수입니다.")
    private Integer winnerCustomerId;

    @NotNull(message = "낙찰 가격은 필수입니다.")
    private Integer finalPrice;

    private String adminNote; // 관리자 메모 (선택 사항)
}