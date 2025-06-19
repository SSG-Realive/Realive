package com.realive.dto.auction;

import com.realive.domain.common.enums.AuctionWinResultType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionResultResponseDTO {
    private final Integer auctionId;                // 경매 ID
    private final AuctionWinResultType resultType;  // 결과(enum): NORMAL_WIN, NO_BID, AUCTION_CANCEL
    private final Integer winningBid;               // 낙찰가 (낙찰 시)
    private final Integer winnerId;                 // 낙찰자 ID (낙찰 시)
    private final LocalDateTime endedAt;            // 경매 종료 시각
    private final String message;                   // 사용자 안내 메시지
}