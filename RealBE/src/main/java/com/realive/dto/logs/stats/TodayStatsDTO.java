package com.realive.dto.logs.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayStatsDTO {
    private long todayOrderCount;     // 오늘 판매 건수
    private double todayRevenue;      // 오늘 매출액
    private LocalDate date;           // 기준 날짜 (오늘)
}
