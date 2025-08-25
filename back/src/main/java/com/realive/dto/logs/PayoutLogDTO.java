package com.realive.dto.logs;

import com.realive.domain.logs.PayoutLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 판매자별 정산 내역
@Getter
@Builder
public class PayoutLogDTO {
    private final Integer id;
    private final Integer sellerId;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final Integer totalSales;
    private final Integer totalCommission;
    private final Integer payoutAmount;
    private final LocalDateTime processedAt;

    public static PayoutLogDTO fromEntity(PayoutLog entity) {
        return PayoutLogDTO.builder()
                .id(entity.getId())
                .sellerId(entity.getSellerId())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .totalSales(entity.getTotalSales())
                .totalCommission(entity.getTotalCommission())
                .payoutAmount(entity.getPayoutAmount())
                .processedAt(entity.getProcessedAt())
                .build();
    }
}
