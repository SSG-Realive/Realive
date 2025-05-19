package com.realive.dto.auction;

import lombok.*;

import java.time.LocalDateTime;

// 입찰 응답
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidResponseDTO {

    private Integer id;
    private Integer auctionId;
    private Integer customerId;
    private Integer bidPrice;
    private LocalDateTime bidTime;

    public static BidResponseDTO fromEntity(com.realive.domain.auction.Bid entity) {
        return BidResponseDTO.builder()
                .id(entity.getId())
                .auctionId(entity.getAuctionId())
                .customerId(entity.getCustomerId())
                .bidPrice(entity.getBidPrice())
                .bidTime(entity.getBidTime())
                .build();
    }
}
