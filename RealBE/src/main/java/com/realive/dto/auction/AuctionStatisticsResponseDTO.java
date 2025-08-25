package com.realive.dto.auction;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

// 경매 현황 및 통계 응답 DTO
@Getter
@Builder
public class AuctionStatisticsResponseDTO {
    private final Integer totalAuctions;           // 전체 경매 수
    private final Integer activeAuctions;          // 진행 중인 경매 수
    private final Integer completedAuctions;       // 완료된 경매 수
    private final Integer totalBids;               // 전체 입찰 수
    private final Double averageBidsPerAuction;    // 경매당 평균 입찰 수
    private final Integer totalRevenue;            // 총 수익
    private final Map<String, Integer> categoryStats; // 카테고리별 경매 수 통계
    private final Map<String, Integer> monthlyStats;  // 월별 경매 수 통계
}