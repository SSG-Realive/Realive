package com.realive.dto.logs;

import lombok.Builder;
import lombok.Getter;

// 개별 판매건과 그에 따른 수수료 내역 DTO
@Getter
@Builder
public class SalesWithCommissionDTO {
    private final SalesLogDTO salesLog; // 개별 판매 로그 DTO
    private final CommissionLogDTO commissionLog; // 해당 판매에 대한 수수료 로그 DTO
}

