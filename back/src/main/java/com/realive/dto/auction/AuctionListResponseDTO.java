package com.realive.dto.auction;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 경매 목록 조회
@Getter
@Builder
public class AuctionListResponseDTO {

    private final Integer id;
    private final Integer productId;
    private final Integer startPrice;
    private final Integer currentPrice;
    private final LocalDateTime endTime;
    private final boolean closed;

    public static AuctionListResponseDTO fromEntity(com.realive.domain.auction.Auction entity) {
        return AuctionListResponseDTO.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .startPrice(entity.getStartPrice())
                .currentPrice(entity.getCurrentPrice())
                .endTime(entity.getEndTime())
                .closed(entity.isClosed())
                .build();
    }
}