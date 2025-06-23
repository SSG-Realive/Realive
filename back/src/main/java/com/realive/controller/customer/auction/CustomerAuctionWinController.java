package com.realive.controller.customer.auction;

import com.realive.dto.auction.AuctionWinResponseDTO;
import com.realive.dto.auction.AuctionPaymentRequestDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.dto.customer.member.MemberLoginDTO;
import com.realive.service.admin.auction.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/customer/auction-wins")
@RequiredArgsConstructor
public class CustomerAuctionWinController {

    private final AuctionService auctionService;

    private Long getAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof MemberLoginDTO)) {
            throw new AccessDeniedException("유효하지 않은 인증 정보입니다.");
        }
        
        return ((MemberLoginDTO) principal).getId();
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionWinResponseDTO>> getAuctionWinInfo(
            @PathVariable Integer auctionId
    ) {
        try {
            Long customerId = getAuthenticatedCustomerId();
            log.info("GET /api/customer/auction-wins/{} - 낙찰 정보 조회, CustomerId: {}", auctionId, customerId);
            
            AuctionWinResponseDTO winInfo = auctionService.getAuctionWinInfo(auctionId, customerId);
            return ResponseEntity.ok(ApiResponse.success(winInfo));
            
        } catch (AccessDeniedException e) {
            log.error("낙찰 정보 조회 권한 없음 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (NoSuchElementException e) {
            log.error("낙찰 정보 조회 실패 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("낙찰 정보 조회 중 알 수 없는 오류 발생 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "낙찰 정보 조회 중 서버 내부 오류가 발생했습니다."));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuctionWinResponseDTO>>> getWonAuctions(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        try {
            Long customerId = getAuthenticatedCustomerId();
            log.info("GET /api/customer/auction-wins - 낙찰한 경매 목록 조회, CustomerId: {}", customerId);
            
            Page<AuctionWinResponseDTO> wonAuctions = auctionService.getWonAuctions(customerId, pageable);
            return ResponseEntity.ok(ApiResponse.success(wonAuctions));
            
        } catch (AccessDeniedException e) {
            log.error("낙찰한 경매 목록 조회 권한 없음", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("낙찰한 경매 목록 조회 중 알 수 없는 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "낙찰한 경매 목록 조회 중 서버 내부 오류가 발생했습니다."));
        }
    }

    @PostMapping("/{auctionId}/payment")
    public ResponseEntity<ApiResponse<Long>> processAuctionPayment(
            @PathVariable Integer auctionId,
            @Valid @RequestBody AuctionPaymentRequestDTO requestDto
    ) {
        try {
            Long customerId = getAuthenticatedCustomerId();
            log.info("POST /api/customer/auction-wins/{}/payment - 경매 결제 처리, CustomerId: {}", auctionId, customerId);
            
            // auctionId가 경로 변수와 요청 DTO의 값이 일치하는지 확인
            if (!auctionId.equals(requestDto.getAuctionId())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "경매 ID가 일치하지 않습니다."));
            }
            
            Long orderId = auctionService.processAuctionPayment(requestDto, customerId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("경매 결제가 성공적으로 처리되었습니다.", orderId));
            
        } catch (AccessDeniedException e) {
            log.error("경매 결제 처리 권한 없음 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (NoSuchElementException e) {
            log.error("경매 결제 처리 실패 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("경매 결제 처리 상태 오류 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("경매 결제 처리 중 알 수 없는 오류 발생 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 결제 처리 중 서버 내부 오류가 발생했습니다."));
        }
    }
} 