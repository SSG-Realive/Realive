package com.realive.dto.logs;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 통합 컨테이너 DTO
@Getter
@Builder
public class AdminDashboardDTO {
    private final ProductLogDTO productLog;
    private final List<PenaltyLogDTO> penaltyLogs;
}
