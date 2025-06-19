package com.realive.dto.logs.salessum;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

// 날짜별 판매 통계
@Getter
@Builder
public class DailySalesSummaryDTO {
    private final LocalDate date;           // 통계 일자
    private final Integer totalSalesCount;  // 총 판매 건수
    private final Integer totalSalesAmount; // 총 판매 금액
    private final Integer totalQuantity;    // 총 수량
}