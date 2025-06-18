package com.realive.dto.bid;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 입찰 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidRequestDTO {

    @NotNull(message = "경매 ID는 필수입니다.")
    private Integer auctionId;

    @NotNull(message = "입찰 가격은 필수입니다.")
    @Positive(message = "입찰 가격은 0보다 커야 합니다.")
    private Integer bidPrice;

    // customerId는 @AuthenticationPrincipal 등으로 서버에서 가져오므로 DTO에 포함하지 않을 수 있음
    // 만약 클라이언트에서 받아야 한다면 추가:
    // @NotNull(message = "고객 ID는 필수입니다.")
    // private Integer customerId;
}
