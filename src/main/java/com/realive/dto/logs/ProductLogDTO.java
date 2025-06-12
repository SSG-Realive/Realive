package com.realive.dto.logs;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 상품 관련 전체 로그 DTO
@Getter
@Builder
public class ProductLogDTO {
    private final List<SalesWithCommissionDTO> salesWithCommissions;
    private final List<PayoutLogDTO> payoutLogs;
}

