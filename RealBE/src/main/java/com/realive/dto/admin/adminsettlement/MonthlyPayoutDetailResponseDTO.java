package com.realive.dto.admin.adminsettlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// 월별 정산 상세 응답 (월별 통계용)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPayoutDetailResponseDTO {
    private String yearMonth;        // "2024-01" 형태
    private Integer totalPayouts;    // 해당 월 정산 건수
    private Integer totalAmount;     // 해당 월 총 정산 금액
    private Integer totalSales;      // 해당 월 총 판매액
    private Integer totalCommission; // 해당 월 총 수수료
    private List<AdminPayoutResponseDTO> payoutDetails; // 해당 월 정산 상세 목록
}