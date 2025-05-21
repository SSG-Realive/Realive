package com.realive.dto.auction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

// 입찰 요청
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidCreateRequestDTO {

    @NotNull(message = "경매 ID는 필수입니다.")
    private Integer auctionId;

    @NotNull(message = "고객 ID는 필수입니다.")
    private Integer customerId;

    @NotNull(message = "입찰 가격은 필수입니다.")
    @Positive(message = "입찰 가격은 0보다 커야 합니다.")
    private Integer bidPrice;
}