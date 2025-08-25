package com.realive.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionPaymentRequestDTO {
    @NotNull
    private Integer auctionId;
    
    @NotNull
    private String paymentKey;

    @NotNull
    private Long amount;
} 