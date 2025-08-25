package com.realive.dto.logs.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentMonthStatsDTO {
    private long currentMonthOrderCount;  // 이번 달 판매 건수
    private double currentMonthRevenue;   // 이번 달 매출액
    private String yearMonth;             // 기준 년월 (YYYY-MM)
}