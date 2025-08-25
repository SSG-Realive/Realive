package com.realive.dto.admin.adminsettlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 일별 정산 요약 응답 (매출 추이 그래프용)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPayoutSummaryResponseDTO {
    private LocalDate date;
    private Long totalAmount;
}