package com.realive.dto.logs;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CommissionLogDTO {

    // 커미션 로그 식별자
    private final Integer id;

    // 관련된 판매 로그 ID (SalesLog와 연결)
    private final Integer salesLogId;

    // 적용된 수수료율
    private final BigDecimal commissionRate;

    // 실제 수수료 금액
    private final Integer commissionAmount;

    // 수수료 적용된 시점
    private final LocalDateTime recordedAt;

    public static CommissionLogDTO fromEntity(com.realive.domain.logs.CommissionLog entity) {
        return CommissionLogDTO.builder()
                .id(entity.getId())
                .salesLogId(entity.getSalesLogId())
                .commissionRate(entity.getCommissionRate())
                .commissionAmount(entity.getCommissionAmount())
                .recordedAt(entity.getRecordedAt())
                .build();
    }
}
