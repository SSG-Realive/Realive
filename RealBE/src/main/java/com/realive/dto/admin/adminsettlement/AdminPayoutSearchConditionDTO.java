package com.realive.dto.admin.adminsettlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 관리자용 정산 검색 조건
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPayoutSearchConditionDTO {
    private String sellerName;      // 판매자명 또는 이메일로 검색
    private LocalDate periodStart;  // 정산 기간 시작
    private LocalDate periodEnd;    // 정산 기간 종료
}
