package com.realive.dto.auction;

import com.realive.domain.common.enums.AuctionStatus;
import lombok.*;
import java.time.LocalDateTime;

// 경매 미리보기 응답 DTO
@Getter
@Builder
public class AuctionPreviewResponseDTO {
    private final Integer auctionId;
    private final Integer productId;
    private final String productName;
    private final String productImage;
    private final Integer startPrice;
    private final Integer currentPrice;
    private final Integer bidCount;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Long remainingTime;  // 초 단위 남은 시간
    private final AuctionStatus status;
}