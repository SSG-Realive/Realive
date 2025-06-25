// src/main/java/com/realive/controller/admin/AdminStatsController.java
package com.realive.controller.admin;

import com.realive.dto.common.ApiResponse;
import com.realive.dto.logs.AdminDashboardDTO;
import com.realive.dto.logs.salessum.CategorySalesSummaryDTO;
import com.realive.dto.logs.salessum.DailySalesSummaryDTO;
import com.realive.dto.logs.salessum.MonthlySalesLogDetailListDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.dto.logs.salessum.SalesLogDetailListDTO;
import com.realive.dto.logs.stats.AuctionPeriodStatsDTO;
import com.realive.dto.logs.stats.MemberPeriodStatsDTO;
import com.realive.dto.logs.stats.MemberSummaryStatsDTO;
import com.realive.dto.logs.stats.DateBasedValueDTO;
import com.realive.dto.logs.stats.MonthBasedValueDTO;
import com.realive.dto.logs.stats.ReviewPeriodStatsDTO;
import com.realive.dto.logs.stats.SalesPeriodStatsDTO; // 수정된 SalesPeriodStatsDTO
import com.realive.dto.logs.stats.SellerSalesDetailDTO; // SellerSalesDetailDTO import 추가
import com.realive.service.admin.logs.StatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin: Statistics", description = "관리자 통계 관련 API")
public class AdminStatsController {

    private final StatService statService;

    @Operation(summary = "관리자 메인 대시보드 통합 정보 조회",
            description = "지정된 날짜와 기간 타입에 따른 관리자 메인 대시보드 정보를 조회합니다. " +
                    "포함 정보: 승인 대기 판매자 수, 상품 관련 로그(판매/수수료, 정산), 패널티 로그, " +
                    "그리고 회원/판매/경매/리뷰에 대한 요약 통계(MemberSummaryStats, SalesSummaryStats 등)를 포함합니다. " +
                    "MemberSummaryStats는 totalMembers, newMembersInPeriod, uniqueVisitorsInPeriod, engagedUsersInPeriod, activeUsersInPeriod를 포함합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminDashboardDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/main-dashboard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getAdminDashboard(
            @Parameter(description = "조회 기준 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "기간 타입 ('DAILY', 'MONTHLY')", example = "DAILY", required = true)
            @RequestParam String periodType) {
        log.info("GET /api/admin/stats/main-dashboard 요청 수신 - date: {}, periodType: {}", date, periodType);
        try {
            AdminDashboardDTO dashboardData = statService.getAdminDashboard(date, periodType);
            return ResponseEntity.ok(ApiResponse.success(dashboardData));
        } catch (Exception e) {
            log.error("관리자 메인 대시보드 조회 중 오류 발생 - date: {}, periodType: {}", date, periodType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류: " + e.getMessage()));
        }
    }

    @Operation(summary = "기간별 판매자 판매 통계 조회", // summary 변경
            description = "지정된 기간 및 조건에 따른 판매자별 판매 건수 및 매출 통계 정보를 조회합니다. " +
                    "SalesPeriodStatsDTO는 전체 판매 요약(선택적), 판매자별 상세 정보 리스트(List<SellerSalesDetailDTO>), " +
                    "그리고 전체 매출 추이(선택적)를 포함합니다. " +
                    "SellerSalesDetailDTO는 sellerId, sellerName, salesCount(판매건수), totalRevenue(총매출)을 포함합니다.") // description 변경
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SalesPeriodStatsDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/sales-period")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<SalesPeriodStatsDTO>> getSalesStatistics(
            @Parameter(description = "조회 시작일 (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일 (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "판매자 ID (선택, 특정 판매자 조회 시)") @RequestParam(required = false) Integer sellerId,
            @Parameter(description = "정렬 기준 (선택, 예: salesCount_desc, revenue_desc)") @RequestParam(required = false) String sortBy) { // sortBy 예시 변경
        log.info("GET /api/admin/stats/sales-period - startDate: {}, endDate: {}, sellerId: {}, sortBy: {}", startDate, endDate, sellerId, sortBy);
        try {
            SalesPeriodStatsDTO stats = statService.getSalesStatistics(startDate, endDate, Optional.ofNullable(sellerId), Optional.ofNullable(sortBy));
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("기간별 판매자 판매 통계 조회 중 오류 발생", e); // 로그 메시지 변경
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류: " + e.getMessage()));
        }
    }

    @Operation(summary = "기간별 경매 참여 통계 조회", description = "지정된 기간 동안의 경매 참여 통계(참여자 수, 평균 입찰수, 낙찰률, 유찰률, 추이)를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuctionPeriodStatsDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/auctions-period")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AuctionPeriodStatsDTO>> getAuctionPeriodStatistics(
            @Parameter(description = "조회 시작일 (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일 (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/admin/stats/auctions-period - startDate: {}, endDate: {}", startDate, endDate);
        try {
            AuctionPeriodStatsDTO stats = statService.getAuctionPeriodStatistics(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("기간별 경매 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류: " + e.getMessage()));
        }
    }

    @Operation(summary = "기간별 회원 통계 조회",
            description = "지정된 기간 동안의 회원 통계를 조회합니다. \n" +
                    "응답 DTO인 MemberPeriodStatsDTO는 다음을 포함합니다: \n" +
                    "- summary: MemberSummaryStatsDTO 객체로, totalMembers, newMembersInPeriod, uniqueVisitorsInPeriod, engagedUsersInPeriod, activeUsersInPeriod 정보를 가집니다. \n" +
                    "- dailyNewUserTrend: 일별 신규 가입자 수 추이 (List<DateBasedValueDTO<Long>>) \n" +
                    "- dailyActiveUserTrend: summary의 activeUsersInPeriod에 대한 일별 추이 (List<DateBasedValueDTO<Long>>) \n" +
                    "- monthlyNewUserTrend: 월별 신규 가입자 수 추이 (List<MonthBasedValueDTO<Long>>) \n" +
                    "- monthlyActiveUserTrend: summary의 activeUsersInPeriod에 대한 월별 추이 (List<MonthBasedValueDTO<Long>>)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MemberPeriodStatsDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/members-period")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MemberPeriodStatsDTO>> getMemberPeriodStatistics(
            @Parameter(description = "조회 시작일 (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일 (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/admin/stats/members-period - startDate: {}, endDate: {}", startDate, endDate);
        try {
            MemberPeriodStatsDTO stats = statService.getMemberPeriodStatistics(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("기간별 회원 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류: " + e.getMessage()));
        }
    }

    @Operation(summary = "기간별 리뷰 통계 조회", description = "지정된 기간 동안의 리뷰 통계(작성된 리뷰 수, 평균 평점, 삭제 리뷰 비율, 평점 분포, 추이)를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewPeriodStatsDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/reviews-period")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ReviewPeriodStatsDTO>> getReviewPeriodStatistics(
            @Parameter(description = "조회 시작일 (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일 (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/admin/stats/reviews-period - startDate: {}, endDate: {}", startDate, endDate);
        try {
            ReviewPeriodStatsDTO stats = statService.getReviewPeriodStatistics(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("기간별 리뷰 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류: " + e.getMessage()));
        }
    }

    // --- 나머지 기존 API 엔드포인트들 (이전과 동일) ---
    @Operation(summary = "관리자 대시보드 통계 조회 (Map 반환 - 레거시)", description = "특정 날짜를 기준으로 대시보드에 필요한 통합 통계 정보를 Map 형태로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MapApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/admin/stats/dashboard 요청 수신 - date: {}", date);
        try {
            Map<String, Object> dashboardStats = statService.getDashboardStats(date);
            if (dashboardStats == null || dashboardStats.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("해당 날짜의 대시보드 데이터가 없습니다.", Collections.emptyMap()));
            }
            return ResponseEntity.ok(ApiResponse.success(dashboardStats));
        } catch (Exception e) {
            log.error("대시보드 통계 조회 중 오류 발생 - date: {}", date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "일별 판매 요약 통계 조회", description = "특정 날짜의 총 판매 건수, 금액, 수량 등을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DailySalesSummaryDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/daily-summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DailySalesSummaryDTO>> getDailySalesSummary(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/admin/stats/daily-summary 요청 수신 - date: {}", date);
        try {
            DailySalesSummaryDTO summary = statService.getDailySalesSummary(date);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            log.error("일별 판매 요약 조회 중 오류 발생 - date: {}", date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "일별 상세 판매 로그 조회", description = "특정 날짜의 모든 판매 로그 상세 내역을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SalesLogDetailListDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/daily-details")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<SalesLogDetailListDTO>> getDailySalesLogDetails(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/admin/stats/daily-details 요청 수신 - date: {}", date);
        try {
            SalesLogDetailListDTO details = statService.getDailySalesLogDetails(date);
            if (details == null || (details.getSalesLogs() != null && details.getSalesLogs().isEmpty())) {
                return ResponseEntity.ok(ApiResponse.success("해당 날짜의 판매 상세 내역이 없습니다.", new SalesLogDetailListDTO(date, Collections.emptyList())));
            }
            return ResponseEntity.ok(ApiResponse.success(details));
        } catch (Exception e) {
            log.error("일별 상세 판매 로그 조회 중 오류 발생 - date: {}", date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "월별 판매 요약 통계 조회", description = "특정 연월의 총 판매 건수, 금액, 수량 등을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MonthlySalesSummaryDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/monthly-summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MonthlySalesSummaryDTO>> getMonthlySalesSummary(
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        log.info("GET /api/admin/stats/monthly-summary 요청 수신 - yearMonth: {}", yearMonth);
        try {
            MonthlySalesSummaryDTO summary = statService.getMonthlySalesSummary(yearMonth);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            log.error("월별 판매 요약 조회 중 오류 발생 - yearMonth: {}", yearMonth, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "월별 상세 판매 로그 조회", description = "특정 연월의 모든 판매 로그 상세 내역을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MonthlySalesLogDetailListDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/monthly-details")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MonthlySalesLogDetailListDTO>> getMonthlySalesLogDetails(
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        log.info("GET /api/admin/stats/monthly-details 요청 수신 - yearMonth: {}", yearMonth);
        try {
            MonthlySalesLogDetailListDTO details = statService.getMonthlySalesLogDetails(yearMonth);
            if (details == null || (details.getSalesLogs() != null && details.getSalesLogs().isEmpty())) {
                return ResponseEntity.ok(ApiResponse.success("해당 연월의 판매 상세 내역이 없습니다.", new MonthlySalesLogDetailListDTO(yearMonth, Collections.emptyList())));
            }
            return ResponseEntity.ok(ApiResponse.success(details));
        } catch (Exception e) {
            log.error("월별 상세 판매 로그 조회 중 오류 발생 - yearMonth: {}", yearMonth, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "카테고리별 판매 요약 조회", description = "특정 기간 동안 플랫폼 전체의 카테고리별 판매 건수 및 금액을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorySalesSummaryDTOListApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/category-summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<CategorySalesSummaryDTO>>> getPlatformCategorySalesSummary(
            @Parameter(description = "조회 시작일 (YYYY-MM-DD)", example = "2025-05-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/admin/stats/category-summary 요청 수신 - startDate: {}, endDate: {}", startDate, endDate);
        try {
            List<CategorySalesSummaryDTO> summary = statService.getPlatformCategorySalesSummary(startDate, endDate);
            if (summary == null || summary.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("해당 기간의 카테고리별 판매 요약 데이터가 없습니다.", Collections.emptyList()));
            }
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            log.error("카테고리별 판매 요약 조회 중 오류 발생 - 기간: {} ~ {}", startDate, endDate, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "특정 월의 일별 판매 요약 리스트 조회", description = "선택한 연월에 해당하는 모든 날짜의 일별 판매 요약 정보를 리스트로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DailySalesSummaryDTOListApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/daily-summaries-in-month")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<DailySalesSummaryDTO>>> getDailySummariesInMonth(
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        log.info("GET /api/admin/stats/daily-summaries-in-month 요청 수신 - yearMonth: {}", yearMonth);
        try {
            List<DailySalesSummaryDTO> summaries = statService.getDailySummariesInMonth(yearMonth);
            if (summaries == null || summaries.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("해당 연월의 일별 판매 요약 데이터가 없습니다.", Collections.emptyList()));
            }
            return ResponseEntity.ok(ApiResponse.success(summaries));
        } catch (Exception e) {
            log.error("특정 월의 일별 판매 요약 리스트 조회 중 오류 발생 - yearMonth: {}", yearMonth, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "특정 판매자의 일별 판매 요약 조회", description = "특정 판매자의 특정 날짜에 대한 판매 요약 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DailySalesSummaryDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/seller/{sellerId}/daily-summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DailySalesSummaryDTO>> getSellerDailySalesSummary(
            @Parameter(description = "조회할 판매자의 ID", example = "1", required = true) @PathVariable Integer sellerId,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/admin/stats/seller/{}/daily-summary 요청 수신 - sellerId: {}, date: {}", sellerId, date);
        try {
            DailySalesSummaryDTO summary = statService.getSellerDailySalesSummary(sellerId, date);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            log.error("판매자 일별 판매 요약 조회 중 오류 발생 - sellerId: {}, date: {}", sellerId, date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "특정 판매자의 월별 판매 요약 조회", description = "특정 판매자의 특정 연월에 대한 판매 요약 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MonthlySalesSummaryDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/seller/{sellerId}/monthly-summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MonthlySalesSummaryDTO>> getSellerMonthlySalesSummary(
            @Parameter(description = "조회할 판매자의 ID", example = "1", required = true) @PathVariable Integer sellerId,
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        log.info("GET /api/admin/stats/seller/{}/monthly-summary 요청 수신 - sellerId: {}, yearMonth: {}", sellerId, yearMonth);
        try {
            MonthlySalesSummaryDTO summary = statService.getSellerMonthlySalesSummary(sellerId, yearMonth);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            log.error("판매자 월별 판매 요약 조회 중 오류 발생 - sellerId: {}, yearMonth: {}", sellerId, yearMonth, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "특정 상품의 일별 판매 요약 조회", description = "특정 상품의 특정 날짜에 대한 판매 요약 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DailySalesSummaryDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/product/{productId}/daily-summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DailySalesSummaryDTO>> getProductDailySalesSummary(
            @Parameter(description = "조회할 상품의 ID", example = "101", required = true) @PathVariable Integer productId,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/admin/stats/product/{}/daily-summary 요청 수신 - productId: {}, date: {}", productId, date);
        try {
            DailySalesSummaryDTO summary = statService.getProductDailySalesSummary(productId, date);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            log.error("상품 일별 판매 요약 조회 중 오류 발생 - productId: {}, date: {}", productId, date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "특정 상품의 월별 판매 요약 조회", description = "특정 상품의 특정 연월에 대한 판매 요약 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MonthlySalesSummaryDTOApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/product/{productId}/monthly-summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MonthlySalesSummaryDTO>> getProductMonthlySalesSummary(
            @Parameter(description = "조회할 상품의 ID", example = "101", required = true) @PathVariable Integer productId,
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        log.info("GET /api/admin/stats/product/{}/monthly-summary 요청 수신 - productId: {}, yearMonth: {}", productId, yearMonth);
        try {
            MonthlySalesSummaryDTO summary = statService.getProductMonthlySalesSummary(productId, yearMonth);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            log.error("상품 월별 판매 요약 조회 중 오류 발생 - productId: {}, yearMonth: {}", productId, yearMonth, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "기간별 월별 판매 요약 리스트 조회",
            description = "지정된 기간 동안의 월별 판매 요약 정보를 리스트로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MonthlySalesSummaryDTOListApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/monthly-summaries-for-period")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<MonthlySalesSummaryDTO>>> getMonthlySummariesForPeriod(
            @Parameter(description = "조회 시작일 (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일 (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /api/admin/stats/monthly-summaries-for-period - startDate: {}, endDate: {}", startDate, endDate);

        try {
            List<MonthlySalesSummaryDTO> summaries = statService.getMonthlySummariesForPeriod(startDate, endDate);
            if (summaries == null || summaries.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("해당 기간의 월별 판매 요약 데이터가 없습니다.", Collections.emptyList()));
            }
            return ResponseEntity.ok(ApiResponse.success(summaries));
        } catch (Exception e) {
            log.error("기간별 월별 판매 요약 조회 중 오류 발생 - 기간: {} ~ {}", startDate, endDate, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    // --- ApiResponse의 Schema 정의를 위한 내부 정적 클래스 ---
    private static class AdminDashboardDTOApiResponse extends ApiResponse<AdminDashboardDTO> {}
    private static class SalesPeriodStatsDTOApiResponse extends ApiResponse<SalesPeriodStatsDTO> {}
    private static class AuctionPeriodStatsDTOApiResponse extends ApiResponse<AuctionPeriodStatsDTO> {}
    private static class MemberPeriodStatsDTOApiResponse extends ApiResponse<MemberPeriodStatsDTO> {}
    private static class ReviewPeriodStatsDTOApiResponse extends ApiResponse<ReviewPeriodStatsDTO> {}
    private static class DailySalesSummaryDTOApiResponse extends ApiResponse<DailySalesSummaryDTO> {}
    private static class SalesLogDetailListDTOApiResponse extends ApiResponse<SalesLogDetailListDTO> {}
    private static class MonthlySalesSummaryDTOApiResponse extends ApiResponse<MonthlySalesSummaryDTO> {}
    private static class MonthlySalesLogDetailListDTOApiResponse extends ApiResponse<MonthlySalesLogDetailListDTO> {}
    private static class CategorySalesSummaryDTOListApiResponse extends ApiResponse<List<CategorySalesSummaryDTO>> {}
    private static class DailySalesSummaryDTOListApiResponse extends ApiResponse<List<DailySalesSummaryDTO>> {}
    private static class MapApiResponse extends ApiResponse<Map<String,Object>>{}
    private static class MonthlySalesSummaryDTOListApiResponse extends ApiResponse<List<MonthlySalesSummaryDTO>> {}
}
