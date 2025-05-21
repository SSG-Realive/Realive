package com.realive.dto.auction.bid;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

// 입찰 수정 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidUpdateRequestDTO {
    @NotNull(message = "입찰 ID는 필수입니다.")
    private Integer bidId;

    @NotNull(message = "고객 ID는 필수입니다.")
    private Integer customerId;

    @NotNull(message = "새 입찰 가격은 필수입니다.")
    @Positive(message = "입찰 가격은 0보다 커야 합니다.")
    private Integer newBidPrice;
}