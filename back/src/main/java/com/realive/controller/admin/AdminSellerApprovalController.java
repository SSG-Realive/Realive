package com.realive.controller.admin;

import com.realive.dto.admin.approval.PendingSellerDTO;
import com.realive.dto.admin.approval.SellerDecisionRequestDTO; // 요청 본문용 DTO
import com.realive.dto.seller.SellerResponseDTO;      // 처리 결과 응답용 DTO
import com.realive.service.admin.approval.SellerApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // @RequestBody, @PathVariable 등

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/admin/sellers") // 컨트롤러의 기본 API 경로
@RequiredArgsConstructor
public class AdminSellerApprovalController {

    private final SellerApprovalService sellerApprovalService;

    /**
     * 1. 승인 대기중인 업체 목록 조회 API
     * 엔드포인트: GET /api/admin/sellers/pending
     * 설명: isApproved == false 이고 approvedAt == null 인 판매자만 조회합니다.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PendingSellerDTO>> getPendingApprovalSellers() {
        log.info("GET /api/admin/sellers/pending - 승인 대기 목록 조회 요청");
        List<PendingSellerDTO> pendingSellers = sellerApprovalService.getPendingApprovalSellers();
        if (pendingSellers.isEmpty()) {
            log.info("승인 대기 중인 판매자가 없습니다.");
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(pendingSellers); // 200 OK
    }

    /**
     * 2. 업체 승인/거부 처리 API (통합 엔드포인트 - "💡 요약 및 흐름" 방식)
     * 엔드포인트: POST /api/admin/sellers/approve
     * 요청 본문: { "sellerId": Long, "approve": boolean }
     * 설명: 특정 판매자를 승인 또는 거부 처리합니다. 모든 승인·거부 시점에는 approvedAt을 현재 시각으로 변경합니다.
     */
    @PostMapping("/approve") // 문서의 "💡 요약 및 흐름"에서 제안된 엔드포인트명
    public ResponseEntity<?> processSellerDecision(@RequestBody SellerDecisionRequestDTO requestDto) {
        // 실제 운영 환경에서는 @AuthenticationPrincipal 등을 통해 현재 로그인한 관리자 ID를 가져옵니다.
        Integer mockAdminId = 0; // 실제로는 인증된 관리자 ID 사용 필요

        log.info("POST /api/admin/sellers/approve - 판매자 처리 요청. SellerId: {}, ApproveAction: {}, AdminId: {}",
                requestDto.getSellerId(), requestDto.isApprove(), mockAdminId);
        try {
            SellerResponseDTO resultSeller = sellerApprovalService.processSellerDecision(
                    requestDto.getSellerId(),
                    requestDto.isApprove(),
                    mockAdminId
            );
            return ResponseEntity.ok(resultSeller); // 200 OK
        } catch (NoSuchElementException e) {
            log.warn("판매자 처리 실패 - ID {} 판매자를 찾을 수 없음: {}", requestDto.getSellerId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (IllegalStateException e) {
            // 서비스 로직에서 "이미 처리된 판매자" 등의 경우 IllegalStateException 발생 가능
            log.warn("판매자 처리 실패 - ID {} 처리 불가: {}", requestDto.getSellerId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict
        } catch (Exception e) {
            log.error("판매자 처리 중 알 수 없는 오류 발생 - ID {}: {}", requestDto.getSellerId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다."); // 500
        }
    }

    @GetMapping("/approved")
    public ResponseEntity<List<SellerResponseDTO>> getApprovedSellers() {
        List<SellerResponseDTO> sellers = sellerApprovalService.getApprovedSellers();
        if (sellers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(sellers);
    }

}
