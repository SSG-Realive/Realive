package com.realive.dto.auction;

import com.realive.dto.auction.bid.BidHistoryDTO;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

// 경매 상세 이력 응답 DTO
@Getter
@Builder
public class AuctionHistoryResponseDTO {
    private final Integer auctionId;
    private final Integer productId;
    private final String productName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Integer startPrice;
    private final Integer finalPrice;
    private final Integer totalBids;
    private final List<BidHistoryDTO> bidHistory;  // 입찰 이력 목록
}