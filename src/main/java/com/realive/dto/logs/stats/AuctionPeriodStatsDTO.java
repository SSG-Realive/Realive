package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AuctionPeriodStatsDTO {
    private AuctionSummaryStatsDTO summary;
    private long averageParticipantsPerAuction; // 경매당 평균 참여 사용자 수 (요구사항: 경매에 참여한 사용자 수)
    // 상세 경매별 통계 리스트 (필요시 추가 가능)
    // private List<AuctionItemStatsDTO> auctionDetails;
    private List<DateBasedValueDTO<Long>> dailyAuctionCountTrend; // 일별 경매 생성 수 추이
    private List<DateBasedValueDTO<Long>> dailyBidCountTrend;     // 일별 입찰 수 추이 (요구사항: 입찰 횟수)
}
