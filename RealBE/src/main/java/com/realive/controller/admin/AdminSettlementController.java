package com.realive.controller.admin;

import com.realive.dto.admin.adminsettlement.*;
import com.realive.service.admin.AdminSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/settlements")  // ← /api 추가
@RequiredArgsConstructor
public class AdminSettlementController {

    private final AdminSettlementService adminSettlementService;

    // 정산 목록 조회 (관리자용)
    @GetMapping
    public ResponseEntity<AdminSettlementPageResponseDTO<AdminPayoutResponseDTO>> getPayoutList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sellerName,
            @RequestParam(required = false) String periodStart,
            @RequestParam(required = false) String periodEnd) {

        PageRequest pageRequest = PageRequest.of(page, size);
        AdminPayoutSearchConditionDTO condition = AdminPayoutSearchConditionDTO.builder()
                .sellerName(sellerName)
                .periodStart(periodStart != null ? LocalDate.parse(periodStart) : null)
                .periodEnd(periodEnd != null ? LocalDate.parse(periodEnd) : null)
                .build();

        AdminSettlementPageResponseDTO<AdminPayoutResponseDTO> response =
                adminSettlementService.getPayoutList(pageRequest, condition);

        return ResponseEntity.ok(response);
    }

    // 정산 상세 조회 (관리자용)
    @GetMapping("/{payoutId:[0-9]+}")
    public ResponseEntity<AdminPayoutDetailResponseDTO> getPayoutDetail(
            @PathVariable Integer payoutId) {

        AdminPayoutDetailResponseDTO response =
                adminSettlementService.getPayoutDetail(payoutId);

        return ResponseEntity.ok(response);
    }

    // 정산 통계 조회 (관리자용)
    @GetMapping("/statistics")
    public ResponseEntity<AdminSettlementStatisticsResponseDTO> getSettlementStatistics() {

        AdminSettlementStatisticsResponseDTO response =
                adminSettlementService.getSettlementStatistics();

        return ResponseEntity.ok(response);
    }

    // 일별 매출 추이 그래프 (관리자용)
    @GetMapping("/trend/daily")
    public ResponseEntity<List<DailyPayoutSummaryResponseDTO>> getDailyPayoutTrend(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<DailyPayoutSummaryResponseDTO> response =
                adminSettlementService.getDailyPayoutSummary(start, end);

        return ResponseEntity.ok(response);
    }

    // 월별 정산 요약 조회 (관리자용)
    @GetMapping("/trend/monthly")
    public ResponseEntity<List<MonthlyPayoutSummaryResponseDTO>> getMonthlyPayoutSummary(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<MonthlyPayoutSummaryResponseDTO> response =
                adminSettlementService.getMonthlyPayoutSummary(start, end);

        return ResponseEntity.ok(response);
    }

    // 월별 정산 상세 조회 (관리자용)
    @GetMapping("/monthly-detail/{yearMonth}")
    public ResponseEntity<MonthlyPayoutDetailResponseDTO> getMonthlyPayoutDetail(
            @PathVariable String yearMonth) {

        MonthlyPayoutDetailResponseDTO response =
                adminSettlementService.getMonthlyPayoutDetail(yearMonth);

        return ResponseEntity.ok(response);
    }
}