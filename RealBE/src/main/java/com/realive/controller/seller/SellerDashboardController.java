package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.logs.stats.CurrentMonthStatsDTO;
import com.realive.dto.logs.stats.TodayStatsDTO;
import com.realive.dto.seller.SellerDashboardResponseDTO;
import com.realive.dto.seller.SellerSalesStatsDTO;
import com.realive.security.seller.SellerPrincipal;
import com.realive.service.seller.SellerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 판매자 대시보드 API
 * - 등록 상품 수
 * - 미답변 QnA 수
 * - 오늘 등록된 상품 수
 * - 총 QnA 수
 * - 진행 중인 주문 수
 */
@RestController
@RequestMapping("/api/seller/dashboard")
@RequiredArgsConstructor
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<SellerDashboardResponseDTO> getDashboard(@AuthenticationPrincipal SellerPrincipal principal) {
        
        SellerDashboardResponseDTO dashboardInfo = dashboardService.getDashboardInfo(principal.getId());
        return ResponseEntity.ok(dashboardInfo);
    }

    // 새로 추가되는 API들
    @GetMapping("/sales-stats")
    public ResponseEntity<SellerSalesStatsDTO> getSalesStatistics(
            @AuthenticationPrincipal SellerPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        SellerSalesStatsDTO salesStats = dashboardService.getSalesStatistics(principal.getId(), startDate, endDate);
        return ResponseEntity.ok(salesStats);
    }

    @GetMapping("/daily-sales-trend")
    public ResponseEntity<List<SellerSalesStatsDTO.DailySalesDTO>> getDailySalesTrend(
            @AuthenticationPrincipal SellerPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<SellerSalesStatsDTO.DailySalesDTO> dailyTrend = dashboardService.getDailySalesTrend(principal.getId(), startDate, endDate);
        return ResponseEntity.ok(dailyTrend);
    }

    @GetMapping("/monthly-sales-trend")
    public ResponseEntity<List<SellerSalesStatsDTO.MonthlySalesDTO>> getMonthlySalesTrend(
            @AuthenticationPrincipal SellerPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<SellerSalesStatsDTO.MonthlySalesDTO> monthlyTrend = dashboardService.getMonthlySalesTrend(principal.getId(), startDate, endDate);
        return ResponseEntity.ok(monthlyTrend);
    }

    @GetMapping("/today-stats")
    public ResponseEntity<TodayStatsDTO> getTodayStats(@AuthenticationPrincipal SellerPrincipal principal) {
        TodayStatsDTO todayStats = dashboardService.getTodayStats(principal.getId());
        return ResponseEntity.ok(todayStats);
    }

    @GetMapping("/current-month-stats")
    public ResponseEntity<CurrentMonthStatsDTO> getCurrentMonthStats(@AuthenticationPrincipal SellerPrincipal principal) {
        CurrentMonthStatsDTO monthStats = dashboardService.getCurrentMonthStats(principal.getId());
        return ResponseEntity.ok(monthStats);
    }
}
