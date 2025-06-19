package com.realive.dto.logs;

import com.realive.domain.logs.PenaltyLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 고객 패널티 이력
@Getter
@Builder
public class PenaltyLogDTO {
    private final Integer id;
    private final Long customerId;
    private final String reason;
    private final Integer points;
    private final String description;
    private final LocalDateTime createdAt;

    public static PenaltyLogDTO fromEntity(PenaltyLog entity) {
        return PenaltyLogDTO.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .reason(entity.getReason())
                .points(entity.getPoints())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
