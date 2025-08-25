package com.realive.dto.logs;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PenaltyLogCreateRequest {
    private Long customerId; // Integer → Long 변경
    private String reason;
    private Integer points;
    private String description;
}