package com.realive.dto.auction;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 입찰 응답
@Getter
@Builder
public class BidResponseDTO {

    private final Integer id;
    private final Integer auctionId;
    private final Integer customerId;
    private final Integer bidPrice;
    private final LocalDateTime bidTime;

    public static BidResponseDTO fromEntity(com.realive.domain.auction.Bid entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Bid 엔티티는 null일 수 없습니다.");
        }

        return BidResponseDTO.builder()
                .id(entity.getId())
                .auctionId(entity.getAuctionId())
                .customerId(entity.getCustomerId())
                .bidPrice(entity.getBidPrice())
                .bidTime(entity.getBidTime())
                .build();
    }
}
