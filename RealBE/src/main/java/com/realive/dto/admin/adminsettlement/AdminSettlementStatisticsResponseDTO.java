package com.realive.dto.admin.adminsettlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 관리자용 정산 통계 응답
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSettlementStatisticsResponseDTO {
    private long totalPayouts;
    private long recentPayouts;
    private Integer totalPayoutAmount;
    private Integer recentPayoutAmount;
}