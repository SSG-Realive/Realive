package com.realive.dto.auction;

import com.realive.domain.common.enums.AuctionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 경매 목록 조회
@Getter
@Builder
public class AuctionListResponseDTO {

    private final Integer id;
    private final Integer startPrice;
    private final Integer currentPrice;
    private final LocalDateTime endTime;
    private final AuctionStatus status;

    public static AuctionListResponseDTO fromEntity(com.realive.domain.auction.Auction entity) {
        return AuctionListResponseDTO.builder()
                .id(entity.getId())
                .startPrice(entity.getStartPrice())
                .currentPrice(entity.getCurrentPrice())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .build();
    }
}