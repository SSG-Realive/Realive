package com.realive.controller.admin.user;

import com.realive.domain.common.enums.ReviewReportStatus;
import com.realive.dto.admin.review.AdminReviewReportDetailDTO;
import com.realive.dto.admin.review.AdminReviewReportListItemDTO; // 이 DTO의 내부 필드명 변경 인지
import com.realive.dto.admin.review.TakeActionOnReportRequestDTO;
import com.realive.dto.admin.review.AdminSellerReviewDetailDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.service.admin.user.AdminReviewService; // 인터페이스 경로

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// import java.util.Optional; // getAllSellerReviews 메소드를 인터페이스에서 주석 처리했다면 이 import도 불필요

@Slf4j
@RestController
@RequestMapping("/api/admin") // 기본 경로 (이전 답변 제안)
@RequiredArgsConstructor
@Tag(name = "Admin: Review & Report Management", description = "관리자 리뷰 및 신고 관리 관련 API")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    // 신고 관련 API들은 /reviews-reports 하위 경로 유지
    @Operation(summary = "신고된 리뷰 목록 조회", description = "특정 상태의 신고된 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminReviewReportListPageApiResponse.class))),
            // ... (다른 응답 코드)
    })
    @GetMapping("/reviews-reports/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminReviewReportListItemDTO>>> getReportedReviews(
            @Parameter(description = "조회할 신고 상태", required = true, example = "PENDING")
            @RequestParam ReviewReportStatus status,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        log.info("GET /api/admin/reviews-reports/reports - status: {}, page: {}, size: {}, sort: {}",
                status, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<AdminReviewReportListItemDTO> reportPage = adminReviewService.getReportedReviewsByStatus(status, pageable);
            return ResponseEntity.ok(ApiResponse.success(reportPage)); // 반환되는 JSON의 키가 reasonSummary -> reason 으로 변경됨
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "잘못된 요청 파라미터입니다: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching reported reviews: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "신고 목록 조회 중 서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "리뷰 신고 상세 정보 조회" /* ... */)
    @GetMapping("/reviews-reports/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminReviewReportDetailDTO>> getReportDetail(
            @PathVariable Integer reportId) {
        // ... (이전 최종본과 동일) ...
        log.info("GET /api/admin/reviews-reports/reports/{}", reportId);
        try {
            AdminReviewReportDetailDTO reportDetail = adminReviewService.getReportDetail(reportId);
            return ResponseEntity.ok(ApiResponse.success(reportDetail));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching review report detail for reportId {}: {}", reportId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "신고 상세 정보 조회 중 서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "리뷰 신고 조치 실행 (상태 변경)" /* ... */)
    @PutMapping("/reviews-reports/reports/{reportId}/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> processReviewReportAction(
            @PathVariable Integer reportId,
            @Valid @RequestBody TakeActionOnReportRequestDTO actionRequest) {
        // ... (이전 최종본과 동일) ...
        log.info("PUT /api/admin/reviews-reports/reports/{}/action - New status: {}", reportId, actionRequest.getNewStatus());
        try {
            adminReviewService.processReportAction(reportId, actionRequest);
            return ResponseEntity.ok(ApiResponse.success("신고 ID " + reportId + "의 상태가 성공적으로 '" + actionRequest.getNewStatus() + "' (으)로 업데이트되었습니다."));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "잘못된 요청 데이터입니다: " + e.getMessage()));
        }
        catch (Exception e) {
            log.error("Error processing action for reportId {}: {}", reportId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "신고 조치 중 서버 오류가 발생했습니다."));
        }
    }

    // 판매자 리뷰 관련 API들은 /seller-reviews 하위 경로로 분리 제안
    @Operation(summary = "판매자 리뷰 상세 정보 조회" /* ... */)
    @GetMapping("/seller-reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminSellerReviewDetailDTO>> getSellerReviewDetail(
            @PathVariable Long reviewId) {
        // ... (이전 최종본과 동일) ...
        log.info("GET /api/admin/seller-reviews/{}", reviewId);
        try {
            AdminSellerReviewDetailDTO reviewDetail = adminReviewService.getSellerReviewDetail(reviewId);
            return ResponseEntity.ok(ApiResponse.success(reviewDetail));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching seller review detail for reviewId {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "리뷰 상세 정보 조회 중 서버 오류가 발생했습니다."));
        }
    }

    // === 인터페이스에서 주석 처리된 메소드들에 대한 컨트롤러 엔드포인트는 현재 없음 ===
    /*
    @GetMapping("/seller-reviews")
    public ResponseEntity<ApiResponse<Page<AdminSellerReviewListItemDTO>>> getAllSellerReviews(...) { ... }
    */

    // --- Swagger 응답 스키마 정의를 위한 내부 정적 클래스 ---
    private static class AdminReviewReportListPageApiResponse extends ApiResponse<Page<AdminReviewReportListItemDTO>> {}
    private static class AdminSellerReviewDetailApiResponse extends ApiResponse<AdminSellerReviewDetailDTO> {}
    private static class AdminReviewReportDetailApiResponse extends ApiResponse<AdminReviewReportDetailDTO> {}
    // private static class AdminSellerReviewListPageApiResponse extends ApiResponse<Page<AdminSellerReviewListItemDTO>> {} // getAllSellerReviews 구현 시 필요
}
