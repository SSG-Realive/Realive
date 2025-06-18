package com.realive.dto.logs.salessum;

import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;

// 월별 판매 요약 DTO
@Getter
@Builder
public class MonthlySalesSummaryDTO {
    private final YearMonth month;          // 통계 월
    private final Integer totalSalesCount;  // 총 판매 건수
    private final Integer totalSalesAmount; // 총 판매 금액
    private final Integer totalQuantity;    // 총 수량
}