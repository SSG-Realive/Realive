package com.realive.dto.bid;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 입찰 이력 DTO
@Getter
@Builder
public class BidHistoryDTO {
    private final Integer bidId;
    private final Long customerId;
    private final String customerName;
    private final Integer bidPrice;
    private final LocalDateTime bidTime;
    private final String bidStatus;  // 입찰 상태 (ACTIVE, CANCELED, OUTBID 등)
}