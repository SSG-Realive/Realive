package com.realive.dto.admin.adminsettlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 수수료 상세 응답
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionDetailResponseDTO {
    private Integer commissionLogId;
    private Integer salesLogId;
    private BigDecimal commissionRate;
    private Integer commissionAmount;
    private LocalDateTime recordedAt;
}