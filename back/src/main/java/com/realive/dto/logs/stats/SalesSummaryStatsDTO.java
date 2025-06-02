package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SalesSummaryStatsDTO {
    private long totalOrdersInPeriod;   // 기간 내 총 주문 건수
    private double totalRevenueInPeriod; // 기간 내 총 매출 합계
    private double totalFeesInPeriod;    // 기간 내 총 수수료 합계
}
