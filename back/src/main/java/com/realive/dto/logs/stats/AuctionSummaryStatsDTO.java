package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuctionSummaryStatsDTO {
    private long totalAuctionsInPeriod;         // 기간 내 총 경매 수
    private long totalBidsInPeriod;             // 기간 내 총 입찰 수
    private double averageBidsPerAuctionInPeriod; // 기간 내 경매당 평균 입찰 수
    private double successRate;                 // 기간 내 낙찰률
    private double failureRate;                 // 기간 내 유찰률 (요구사항: 유찰률)
    // 요구사항에는 '상품 수'도 있었으나, 이는 totalAuctionsInPeriod와 유사하거나,
    // 경매 대상이 된 유니크한 상품의 수일 수 있습니다. 필요시 추가.
}
