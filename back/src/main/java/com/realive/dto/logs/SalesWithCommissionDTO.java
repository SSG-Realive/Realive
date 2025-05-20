package com.realive.dto.logs;

import lombok.Builder;
import lombok.Getter;

// 개별 판매건과 그에 따른 수수료 내역 DTO
@Getter
@Builder
public class SalesWithCommissionDTO {
    private final SalesLogDTO salesLog;
    private final CommissionLogDTO commissionLog;
}

