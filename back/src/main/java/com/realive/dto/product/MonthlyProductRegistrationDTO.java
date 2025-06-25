package com.realive.dto.product;

import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;

/**
 * 월별 상품 등록 통계 DTO
 */
@Getter
@Builder
public class MonthlyProductRegistrationDTO {
    private final YearMonth yearMonth;  // 연월 (예: 2024-01)
    private final Integer count;        // 해당 월 등록된 상품 수
} 