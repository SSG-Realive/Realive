package com.realive.dto.logs; // 또는 com.realive.dto.admin 등 적절한 패키지


import com.realive.dto.logs.stats.AuctionSummaryStatsDTO;
import com.realive.dto.logs.stats.MemberSummaryStatsDTO;
import com.realive.dto.logs.stats.ReviewSummaryStatsDTO;
import com.realive.dto.logs.stats.SalesSummaryStatsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {

    // --- 대시보드 조회 기준 정보 ---
    private LocalDate queryDate;    // 조회 기준일 (또는 기간 시작일)
    private String periodType;      // 조회 기간 타입 (e.g., "DAILY", "MONTHLY")

    private int pendingSellerCount;         // 승인 대기 중인 판매자 수
    private ProductLogDTO productLog;       // (사용자 제공 DTO)
    private List<PenaltyLogDTO> penaltyLogs; // (사용자 제공 DTO)

    private MemberSummaryStatsDTO memberSummaryStats;
    private SalesSummaryStatsDTO salesSummaryStats;
    private AuctionSummaryStatsDTO auctionSummaryStats;
    private ReviewSummaryStatsDTO reviewSummaryStats;
}
