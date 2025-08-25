package com.realive.dto.admin.adminsettlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 관리자용 정산 상세 응답
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPayoutDetailResponseDTO {
    private Integer payoutId;
    private Integer sellerId;
    private String sellerName;
    private String sellerEmail;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer totalSales;
    private Integer totalCommission;
    private Integer payoutAmount;
    private LocalDateTime processedAt;
    private List<SalesDetailResponseDTO> salesDetails;
    private List<CommissionDetailResponseDTO> commissionDetails;
}