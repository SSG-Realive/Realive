package com.realive.dto.auction;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionCancelResponseDTO {
    private final Integer auctionId;         // 대상 경매 ID
    private final boolean cancelled;         // 취소/중단 성공 여부
    private final String reason;             // 취소 사유 (옵션)
    private final LocalDateTime cancelledAt; // 취소 요청 시각
    private final Integer cancelledBy;       // 취소/중단한 사용자 ID 또는 시스템
    private final String message;            // 안내 메시지
}