package com.realive.dto.auction;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidDTO {
    

    private Integer id;

    @NotNull(message = "경매 ID는 필수입니다.")
    private Integer auctionId;

    @NotNull(message = "고객 ID는 필수입니다.")
    private Integer customerId;

    @Positive(message = "입찰가는 0보다 커야 합니다.")
    private Integer bidPrice;

    private LocalDateTime bidTime;
}
