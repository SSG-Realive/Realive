package com.realive.dto.auction;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductDTO {
    
    private Integer id;

    private Integer purchaseId;

    private Integer purchasePrice;

    private Integer purchasedFromSellerId;

    private LocalDateTime purchasedAt;

    private boolean isAuctioned;


}
