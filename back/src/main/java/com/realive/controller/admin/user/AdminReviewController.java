package com.realive.controller.admin.user;


import com.realive.domain.common.enums.ReviewReportStatus;
import com.realive.dto.admin.review.AdminReviewReportListItemDTO;
import com.realive.dto.admin.review.AdminSellerReviewDetailDTO;
import com.realive.dto.common.ApiResponse; // 사용자 정의 공통 응답 DTO
import com.realive.service.admin.user.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/reviews-reports") // 이 컨트롤러의 API 기본 경로
@RequiredArgsConstructor
@Tag(name = "Admin: Review & Report Management", description = "관리자 리뷰 및 신고 관리 관련 API")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @Operation(summary = "신고된 리뷰 목록 조회",
            description = "특정 상태(예: PENDING)의 신고된 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminReviewReportListPageApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))), // 공통 ApiResponse 사용
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/reports") // 최종 API 경로: /api/admin/reviews-reports/reports
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminReviewReportListItemDTO>>> getReportedReviews(
            @Parameter(description = "조회할 신고의 처리 상태.", required = true, example = "PENDING")
            @RequestParam ReviewReportStatus status,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        log.info("GET /api/admin/reviews-reports/reports - status: {}, page: {}, size: {}, sort: {}",
                status, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<AdminReviewReportListItemDTO> reportPage = adminReviewService.getReportedReviewsByStatus(status, pageable);
            return ResponseEntity.ok(ApiResponse.success(reportPage));
        } catch (IllegalArgumentException e) { // 잘못된 Enum 값 변환 등
            log.warn("Bad request for reported reviews: Invalid status value or other argument. {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "잘못된 요청 파라미터입니다: " + e.getMessage()));
        }
        catch (Exception e) {
            log.error("Error fetching reported reviews: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "신고 목록 조회 중 서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "판매자 리뷰 상세 정보 조회",
            description = "특정 판매자 리뷰의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 상세 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminSellerReviewDetailApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/seller-reviews/{reviewId}") // 최종 API 경로: /api/admin/reviews-reports/seller-reviews/{reviewId}
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminSellerReviewDetailDTO>> getSellerReviewDetail(
            @Parameter(description = "조회할 판매자 리뷰의 ID", required = true, example = "1")
            @PathVariable Long reviewId) { // SellerReview.id는 Long 타입
        log.info("GET /api/admin/reviews-reports/seller-reviews/{}", reviewId);
        try {
            AdminSellerReviewDetailDTO reviewDetail = adminReviewService.getSellerReviewDetail(reviewId);
            return ResponseEntity.ok(ApiResponse.success(reviewDetail));
        } catch (EntityNotFoundException e) {
            log.warn("SellerReview not found for detail view: reviewId={}, error: {}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching seller review detail for reviewId {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "리뷰 상세 정보 조회 중 서버 오류가 발생했습니다."));
        }
    }

    // --- Swagger 응답 스키마 정의를 위한 내부 정적 클래스 ---
    private static class AdminReviewReportListPageApiResponse extends ApiResponse<Page<AdminReviewReportListItemDTO>> {}
    private static class AdminSellerReviewDetailApiResponse extends ApiResponse<AdminSellerReviewDetailDTO> {}

    // --- 향후 "리뷰/신고 관리" Todo 리스트에 따라 추가될 API 엔드포인트들 ---
    // (예: 신고 상세 조회, 신고 조치, 리뷰 목록 조회, 리뷰 상태 변경, 리뷰 삭제 등)
}
