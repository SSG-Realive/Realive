package com.realive.dto.logs;

import lombok.Getter;

@Getter
public class UpdatePayoutStatusRequestDTO {
    private String newStatus; // 예: "COMPLETED"
    private String adminMemo; // 관리자 메모 (선택)
}