package com.realive.dto.admin.adminsettlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 월별 정산 요약 응답 (월별 매출 추이 그래프용)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPayoutSummaryResponseDTO {
    private String yearMonth;        // "2024-01" 형태
    private Integer totalPayouts;    // 해당 월 정산 건수
    private Integer totalAmount;     // 해당 월 총 정산 금액
}