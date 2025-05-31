package com.realive.controller.admin.user;

import com.realive.domain.common.enums.ReviewReportStatus;
import com.realive.dto.admin.review.*;
import com.realive.dto.common.ApiResponse;
import com.realive.service.admin.user.AdminReviewService;

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

import java.util.Optional;

/**
 * 관리자의 리뷰 및 신고 관리 기능을 위한 REST 컨트롤러입니다.
 * (Javadoc 주석은 이전 최종본과 동일하게 유지)
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin: Review & Report Management", description = "관리자 리뷰 및 신고 관리 관련 API")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    // --- getReportedReviews, getReportDetail, processReviewReportAction, getSellerReviewDetail, getAllSellerReviews 메소드는 이전 최종본과 동일 ---
    // (이 메소드들은 서비스 계층에서 반환하는 DTO에 상품 정보가 추가되었더라도, API 스펙 자체는 변경되지 않음)

    @Operation(summary = "신고된 리뷰 목록 조회"/* ... */)
    @GetMapping("/reviews-reports/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminReviewReportListItemDTO>>> getReportedReviews(
            @Parameter(description = "조회할 리뷰 신고의 처리 상태 (예: PENDING, RESOLVED_KEPT)")
            @RequestParam ReviewReportStatus status,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        // ... (이전 최종본과 동일)
        log.info("GET /api/admin/reviews-reports/reports - status: {}, page: {}, size: {}, sort: {}",
                status, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<AdminReviewReportListItemDTO> reportPage = adminReviewService.getReportedReviewsByStatus(status, pageable);
            return ResponseEntity.ok(ApiResponse.success(reportPage));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "잘못된 요청 파라미터입니다: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching reported reviews: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "신고 목록 조회 중 서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "리뷰 신고 상세 정보 조회"/* ... */)
    @GetMapping("/reviews-reports/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminReviewReportDetailDTO>> getReportDetail(
            @Parameter(description = "조회할 리뷰 신고의 ID")
            @PathVariable Integer reportId) {
        // ... (이전 최종본과 동일)
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

    @Operation(summary = "리뷰 신고 조치 실행"/* ... */)
    @PutMapping("/reviews-reports/reports/{reportId}/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> processReviewReportAction(
            @Parameter(description = "조치를 취할 리뷰 신고의 ID")
            @PathVariable Integer reportId,
            @Parameter(description = "새로운 신고 처리 상태 정보", required = true,
                    content = @Content(schema = @Schema(implementation = TakeActionOnReportRequestDTO.class)))
            @Valid @RequestBody TakeActionOnReportRequestDTO actionRequest) {
        // ... (이전 최종본과 동일)
        log.info("PUT /api/admin/reviews-reports/reports/{}/action - New status: {}", reportId, actionRequest.getNewStatus());
        try {
            adminReviewService.processReportAction(reportId, actionRequest);
            return ResponseEntity.ok(ApiResponse.success("신고 ID " + reportId + "의 상태가 성공적으로 '" + actionRequest.getNewStatus() + "' (으)로 업데이트되었습니다."));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "잘못된 요청 데이터입니다: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing action for reportId {}: {}", reportId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "신고 조치 중 서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "판매자 리뷰 상세 정보 조회"/* ... */)
    @GetMapping("/seller-reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminSellerReviewDetailDTO>> getSellerReviewDetail(
            @Parameter(description = "조회할 판매자 리뷰의 ID")
            @PathVariable Long reviewId) {
        // ... (이전 최종본과 동일)
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

    @Operation(summary = "전체 판매자 리뷰 목록 조회 (필터링 가능)"/* ... */)
    @GetMapping("/seller-reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminSellerReviewListItemDTO>>> getAllSellerReviews(
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable,
            @Parameter(description = "상품명 필터 (선택 사항)") @RequestParam(required = false) Optional<String> productFilter,
            @Parameter(description = "고객명 필터 (선택 사항)") @RequestParam(required = false) Optional<String> customerFilter,
            @Parameter(description = "판매자명 필터 (선택 사항)") @RequestParam(required = false) Optional<String> sellerFilter) {
        // ... (이전 최종본과 동일)
        log.info("GET /api/admin/seller-reviews - Page: {}, Size: {}, Sort: {}, ProductFilter: {}, CustomerFilter: {}, SellerFilter: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(),
                productFilter.orElse("N/A"), customerFilter.orElse("N/A"), sellerFilter.orElse("N/A"));
        try {
            Page<AdminSellerReviewListItemDTO> reviewPage = adminReviewService.getAllSellerReviews(pageable, productFilter, customerFilter, sellerFilter);
            return ResponseEntity.ok(ApiResponse.success(reviewPage));
        } catch (Exception e) {
            log.error("Error fetching all seller reviews: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "리뷰 목록 조회 중 서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "판매자 리뷰 숨김/공개 상태 변경"/* ... */)
    @ApiResponses(value = {
            // ... (Swagger 응답 정의는 이전 최종본과 동일)
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 상태 변경 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminReviewController.StringApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 값 (예: isHidden 필드 누락 또는 유효하지 않은 값)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 ID의 리뷰를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음 (관리자 권한 필요)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @PutMapping("/seller-reviews/{reviewId}/visibility")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateSellerReviewVisibility(
            @Parameter(description = "상태를 변경할 판매자 리뷰의 ID", required = true, example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "새로운 숨김 상태 정보 (JSON 형식: {\"isHidden\": true} 또는 {\"isHidden\": false})", required = true,
                    content = @Content(schema = @Schema(implementation = UpdateReviewVisibilityRequestDTO.class)))
            @Valid @RequestBody UpdateReviewVisibilityRequestDTO requestDTO) {
        // ... (이전 최종본과 동일)
        log.info("PUT /api/admin/seller-reviews/{}/visibility - New isHidden status: {}", reviewId, requestDTO.getIsHidden());
        try {
            adminReviewService.updateSellerReviewVisibility(reviewId, requestDTO.getIsHidden());
            String message = String.format("리뷰 ID %d의 숨김 상태가 '%s'(으)로 성공적으로 업데이트되었습니다.", reviewId, requestDTO.getIsHidden() ? "숨김" : "공개");
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (EntityNotFoundException e) {
            log.warn("SellerReview not found for visibility update with ID: {}. Error: {}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument for updating review visibility for reviewId {}. Error: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating visibility for seller review ID {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "리뷰 상태 변경 중 서버 오류가 발생했습니다."));
        }
    }

    // --- Swagger 응답 스키마 정의를 위한 내부 정적 클래스들 (선택적) ---
    private static class AdminReviewReportListPageApiResponse extends ApiResponse<Page<AdminReviewReportListItemDTO>> {}
    private static class AdminSellerReviewDetailApiResponse extends ApiResponse<AdminSellerReviewDetailDTO> {}
    private static class AdminReviewReportDetailApiResponse extends ApiResponse<AdminReviewReportDetailDTO> {}
    private static class AdminSellerReviewListPageApiResponse extends ApiResponse<Page<AdminSellerReviewListItemDTO>> {}
    private static class StringApiResponse extends ApiResponse<String> {}
}
