package com.realive.dto.product;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 일별 상품 등록 통계 DTO
 */
@Getter
@Builder
public class DailyProductRegistrationDTO {
    private final LocalDate date;  // 날짜 (예: 2024-06-15)
    private final Integer count;   // 해당 일 등록된 상품 수
} 