package com.realive.controller.admin; // 또는 com.realive.controller.stats 등 적절한 패키지

import com.realive.dto.common.ApiResponse; // ApiResponse DTO import
import com.realive.dto.logs.salessum.CategorySalesSummaryDTO;
import com.realive.dto.logs.salessum.DailySalesSummaryDTO;
import com.realive.dto.logs.salessum.MonthlySalesLogDetailListDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.dto.logs.salessum.SalesLogDetailListDTO;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin: Statistics", description = "관리자 통계 관련 API")
public class StatController {

    private final StatService statService;

    // --- 기존 엔드포인트들 (getDashboardStats, 일별/월별 요약 및 상세, 카테고리별 요약) ---
    // 예시:
    @Operation(summary = "관리자 대시보드 통계 조회", description = "특정 날짜를 기준으로 대시보드에 필요한 통합 통계 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "데이터 없음", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/admin/stats/dashboard 요청 수신 - date: {}", date);
        try {
            Map<String, Object> dashboardStats = statService.getDashboardStats(date);
            if (dashboardStats == null || dashboardStats.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("데이터가 없습니다.", null));
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "데이터 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/daily-summary")
    public ResponseEntity<ApiResponse<DailySalesSummaryDTO>> getDailySalesSummary(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/admin/stats/daily-summary 요청 수신 - date: {}", date);
        try {
            DailySalesSummaryDTO summary = statService.getDailySalesSummary(date);
            if (summary == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "해당 날짜의 판매 요약 데이터가 없습니다."));
            }
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "데이터 없음", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/daily-details")
    public ResponseEntity<ApiResponse<SalesLogDetailListDTO>> getDailySalesLogDetails(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/admin/stats/daily-details 요청 수신 - date: {}", date);
        try {
            SalesLogDetailListDTO details = statService.getDailySalesLogDetails(date);
            if (details == null || (details.getSalesLogs() != null && details.getSalesLogs().isEmpty())) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("해당 날짜의 판매 상세 내역이 없습니다.", null));
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "데이터 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/monthly-summary")
    public ResponseEntity<ApiResponse<MonthlySalesSummaryDTO>> getMonthlySalesSummary(
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        log.info("GET /api/admin/stats/monthly-summary 요청 수신 - yearMonth: {}", yearMonth);
        try {
            MonthlySalesSummaryDTO summary = statService.getMonthlySalesSummary(yearMonth);
            if (summary == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "해당 연월의 판매 요약 데이터가 없습니다."));
            }
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "데이터 없음", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/monthly-details")
    public ResponseEntity<ApiResponse<MonthlySalesLogDetailListDTO>> getMonthlySalesLogDetails(
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        log.info("GET /api/admin/stats/monthly-details 요청 수신 - yearMonth: {}", yearMonth);
        try {
            MonthlySalesLogDetailListDTO details = statService.getMonthlySalesLogDetails(yearMonth);
            if (details == null || (details.getSalesLogs() != null && details.getSalesLogs().isEmpty())) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("해당 연월의 판매 상세 내역이 없습니다.", null));
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "데이터 없음", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/category-summary")
    public ResponseEntity<ApiResponse<List<CategorySalesSummaryDTO>>> getPlatformCategorySalesSummary(
            @Parameter(description = "조회 시작일 (YYYY-MM-DD)", example = "2025-05-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/admin/stats/category-summary 요청 수신 - startDate: {}, endDate: {}", startDate, endDate);
        try {
            List<CategorySalesSummaryDTO> summary = statService.getPlatformCategorySalesSummary(startDate, endDate);
            if (summary == null || summary.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("해당 기간의 카테고리별 판매 요약 데이터가 없습니다.", null));
            }
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            log.error("카테고리별 판매 요약 조회 중 오류 발생 - 기간: {} ~ {}", startDate, endDate, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }

    // --- 주석 처리되었던 엔드포인트들 구현 ---

    @Operation(summary = "특정 월의 일별 판매 요약 리스트 조회", description = "선택한 연월에 해당하는 모든 날짜의 일별 판매 요약 정보를 리스트로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))), // ApiResponse<List<DailySalesSummaryDTO>>
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "데이터 없음", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/daily-summaries-in-month")
    public ResponseEntity<ApiResponse<List<DailySalesSummaryDTO>>> getDailySummariesInMonth(
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        log.info("GET /api/admin/stats/daily-summaries-in-month 요청 수신 - yearMonth: {}", yearMonth);
        try {
            List<DailySalesSummaryDTO> summaries = statService.getDailySummariesInMonth(yearMonth);
            if (summaries == null || summaries.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("해당 연월의 일별 판매 요약 데이터가 없습니다.", null));
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))), // ApiResponse<DailySalesSummaryDTO>
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "데이터 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/seller/{sellerId}/daily-summary")
    public ResponseEntity<ApiResponse<DailySalesSummaryDTO>> getSellerDailySalesSummary(
            @Parameter(description = "조회할 판매자의 ID", example = "1", required = true) @PathVariable Integer sellerId,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/admin/stats/seller/{}/daily-summary 요청 수신 - sellerId: {}, date: {}", sellerId, date);
        try {
            DailySalesSummaryDTO summary = statService.getSellerDailySalesSummary(sellerId, date);
            if (summary == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "해당 판매자 또는 날짜의 판매 요약 데이터가 없습니다."));
            }
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))), // ApiResponse<MonthlySalesSummaryDTO>
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "데이터 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/seller/{sellerId}/monthly-summary")
    public ResponseEntity<ApiResponse<MonthlySalesSummaryDTO>> getSellerMonthlySalesSummary(
            @Parameter(description = "조회할 판매자의 ID", example = "1", required = true) @PathVariable Integer sellerId,
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        log.info("GET /api/admin/stats/seller/{}/monthly-summary 요청 수신 - sellerId: {}, yearMonth: {}", sellerId, yearMonth);
        try {
            MonthlySalesSummaryDTO summary = statService.getSellerMonthlySalesSummary(sellerId, yearMonth);
            if (summary == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "해당 판매자 또는 연월의 판매 요약 데이터가 없습니다."));
            }
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))), // ApiResponse<DailySalesSummaryDTO>
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "데이터 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/product/{productId}/daily-summary")
    public ResponseEntity<ApiResponse<DailySalesSummaryDTO>> getProductDailySalesSummary(
            @Parameter(description = "조회할 상품의 ID", example = "101", required = true) @PathVariable Integer productId,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-27", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/admin/stats/product/{}/daily-summary 요청 수신 - productId: {}, date: {}", productId, date);
        try {
            DailySalesSummaryDTO summary = statService.getProductDailySalesSummary(productId, date);
            if (summary == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "해당 상품 또는 날짜의 판매 요약 데이터가 없습니다."));
            }
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))), // ApiResponse<MonthlySalesSummaryDTO>
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "데이터 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/product/{productId}/monthly-summary")
    public ResponseEntity<ApiResponse<MonthlySalesSummaryDTO>> getProductMonthlySalesSummary(
            @Parameter(description = "조회할 상품의 ID", example = "101", required = true) @PathVariable Integer productId,
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        log.info("GET /api/admin/stats/product/{}/monthly-summary 요청 수신 - productId: {}, yearMonth: {}", productId, yearMonth);
        try {
            MonthlySalesSummaryDTO summary = statService.getProductMonthlySalesSummary(productId, yearMonth);
            if (summary == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "해당 상품 또는 연월의 판매 요약 데이터가 없습니다."));
            }
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            log.error("상품 월별 판매 요약 조회 중 오류 발생 - productId: {}, yearMonth: {}", productId, yearMonth, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
    }
}
