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

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin: Review & Report Management", description = "관리자 리뷰 및 신고 관리 관련 API")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping("/reviews-reports/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminReviewReportListItemDTO>>> getReportedReviews( // 제네릭 타입 명시
                                                                                               @RequestParam ReviewReportStatus status,
                                                                                               @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
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
    } // getReportedReviews 닫는 중괄호

    @GetMapping("/reviews-reports/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminReviewReportDetailDTO>> getReportDetail( // 제네릭 타입 명시
                                                                                    @PathVariable Integer reportId) {
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
    } // getReportDetail 닫는 중괄호

    @PutMapping("/reviews-reports/reports/{reportId}/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> processReviewReportAction( // 제네릭 타입 명시
                                                                          @PathVariable Integer reportId,
                                                                          @Valid @RequestBody TakeActionOnReportRequestDTO actionRequest) {
        log.info("PUT /api/admin/reviews-reports/reports/{}/action - New status: {}", reportId, actionRequest.getNewStatus());
        try {
            adminReviewService.processReportAction(reportId, actionRequest); // 이전 단계에서 메소드명 processReportAction으로 통일
            return ResponseEntity.ok(ApiResponse.success("신고 ID " + reportId + "의 상태가 성공적으로 '" + actionRequest.getNewStatus() + "' (으)로 업데이트되었습니다."));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "잘못된 요청 데이터입니다: " + e.getMessage()));
        } catch (Exception e) { // 사용자 첨부 파일에서 catch 블록 중괄호 문제 수정
            log.error("Error processing action for reportId {}: {}", reportId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "신고 조치 중 서버 오류가 발생했습니다."));
        }
    } // processReviewReportAction 닫는 중괄호

    @GetMapping("/seller-reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminSellerReviewDetailDTO>> getSellerReviewDetail( // 제네릭 타입 명시
                                                                                          @PathVariable Long reviewId) {
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
    } // getSellerReviewDetail 닫는 중괄호

    // getAllSellerReviews 엔드포인트 추가 (사용자 파일에 누락되어 있었음)
    @Operation(summary = "전체 판매자 리뷰 목록 조회 (필터링 가능)",
            description = "모든 판매자 리뷰 목록을 페이징하여 조회합니다. 상품명, 고객명, 또는 판매자명으로 필터링할 수 있습니다.")
    @GetMapping("/seller-reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminSellerReviewListItemDTO>>> getAllSellerReviews(
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable,
            @RequestParam(required = false) Optional<String> productFilter,
            @RequestParam(required = false) Optional<String> customerFilter,
            @RequestParam(required = false) Optional<String> sellerFilter) {
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
    } // getAllSellerReviews 닫는 중괄호

    // === "리뷰 삭제 API"를 위한 엔드포인트 추가 ===
    @Operation(summary = "판매자 리뷰 삭제",
            description = "관리자가 특정 판매자 리뷰를 시스템에서 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "삭제할 리뷰를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/seller-reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteSellerReview(
            @Parameter(description = "삭제할 판매자 리뷰의 ID", required = true, example = "1")
            @PathVariable Long reviewId) {
        log.info("DELETE /api/admin/seller-reviews/{}", reviewId);
        try {
            adminReviewService.deleteSellerReview(reviewId);
            return ResponseEntity.ok(ApiResponse.success("리뷰 ID " + reviewId + "이(가) 성공적으로 삭제되었습니다."));
        } catch (EntityNotFoundException e) {
            log.warn("SellerReview not found for deletion with ID: {}. Error: {}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting seller review with ID {}: {}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "리뷰 삭제 중 서버 오류가 발생했습니다."));
        }
    } // deleteSellerReview 닫는 중괄호

    // --- Swagger 응답 스키마 정의를 위한 내부 정적 클래스들 ---
    private static class AdminReviewReportListPageApiResponse extends ApiResponse<Page<AdminReviewReportListItemDTO>> {}
    private static class AdminSellerReviewDetailApiResponse extends ApiResponse<AdminSellerReviewDetailDTO> {}
    private static class AdminReviewReportDetailApiResponse extends ApiResponse<AdminReviewReportDetailDTO> {}
    private static class AdminSellerReviewListPageApiResponse extends ApiResponse<Page<AdminSellerReviewListItemDTO>> {} // getAllSellerReviews 구현 시 필요
}
