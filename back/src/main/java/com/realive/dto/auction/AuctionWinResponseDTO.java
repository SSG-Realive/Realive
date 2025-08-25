package com.realive.dto.auction;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionWinResponseDTO {
    private final Integer auctionId;
    private final String productName;
    private final String productImageUrl;
    private final Integer winningBidPrice;
    private final LocalDateTime auctionEndTime;
    private final LocalDateTime paymentDeadline; // 결제 마감일 (예: 경매 종료 후 7일)
    private final boolean isPaid; // 결제 완료 여부
    private final String paymentStatus; // 결제 상태
    private final boolean isNewWin; // 새로 낙찰된 상품인지 여부 (알림용)
    private final String winMessage; // 낙찰 축하 메시지
} 