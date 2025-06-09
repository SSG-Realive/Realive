package com.realive.controller.admin.auction;

import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.security.AdminPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/admin/auctions")
@RequiredArgsConstructor
public class AdminAuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> registerAuction(
            @Valid @RequestBody AuctionCreateRequestDTO requestDto,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal
    ) {
        log.info("POST /api/admin/auctions - 관리자 경매 등록 요청. DTO: {}, AdminId: {}",
                requestDto, (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"));

        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        Long adminUserId = Long.valueOf(String.valueOf(adminPrincipal.getAdmin().getId()));

        try {
            AuctionResponseDTO registeredAuction = auctionService.registerAuction(requestDto, adminUserId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("경매가 성공적으로 등록되었습니다.", registeredAuction));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("관리자 경매 등록 중 알 수 없는 서버 내부 오류 발생. AdminId: {}, DTO: {}", adminUserId, requestDto, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 등록 중 서버 내부 오류가 발생했습니다."));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuctionResponseDTO>>> getActiveAuctions(
            @PageableDefault(size = 10, sort = "endTime,asc") Pageable pageable, // 기본 페이징: 10개씩, 마감시간 오름차순
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal
    ) {
        log.info("GET /api/admin/auctions - 관리자 경매 목록 조회. AdminId: {}, Category: {}, Status: {}",
                (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"), category, status);
        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        try {
            Page<AuctionResponseDTO> activeAuctions = auctionService.getActiveAuctions(pageable, category, status);
            if (activeAuctions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK) // 결과가 없어도 200 OK
                        .body(ApiResponse.success("조건에 맞는 경매가 없습니다.", activeAuctions));
            }
            return ResponseEntity.ok(ApiResponse.success(activeAuctions));
        } catch (Exception e) {
            log.error("관리자 경매 목록 조회 중 알 수 없는 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 목록 조회 중 서버 내부 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> getAuctionDetails(
            @PathVariable Integer auctionId,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal
    ) {
        log.info("GET /api/admin/auctions/{} - 관리자 경매 상세 정보 조회. AdminId: {}",
                auctionId, (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"));
        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        try {
            AuctionResponseDTO auctionDetails = auctionService.getAuctionDetails(auctionId);
            return ResponseEntity.ok(ApiResponse.success(auctionDetails));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("관리자 경매 상세 정보(AuctionId:{}) 조회 중 알 수 없는 오류 발생", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 상세 정보 조회 중 서버 내부 오류가 발생했습니다."));
        }
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<Page<AuctionResponseDTO>>> getAuctionsBySeller(
            @PathVariable Long sellerId,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable, // 기본: 등록시간 내림차순
            @AuthenticationPrincipal AdminPrincipal adminPrincipal
    ) {
        log.info("GET /api/admin/auctions/seller/{} - 관리자가 특정 판매자 경매 목록 조회. AdminId: {}",
                sellerId, (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"));
        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        try {
            Page<AuctionResponseDTO> sellerAuctions = auctionService.getAuctionsBySeller(sellerId, pageable);
            if (sellerAuctions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success("해당 판매자가 등록한 경매가 없습니다.", sellerAuctions));
            }
            return ResponseEntity.ok(ApiResponse.success(sellerAuctions));
        } catch (Exception e) {
            log.error("관리자가 판매자(ID:{}) 경매 목록 조회 중 오류 발생: {}", sellerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "판매자 경매 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/{auctionId}/cancel")
    public ResponseEntity<ApiResponse<AuctionCancelResponseDTO>> cancelAuction(
            @PathVariable Integer auctionId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal
    ) {
        log.info("POST /api/admin/auctions/{}/cancel - 관리자 경매 취소 요청. AdminId: {}, 사유: {}",
                auctionId, (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"), reason);

        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }

        try {
            AuctionCancelResponseDTO result = auctionService.cancelAuction(auctionId, adminPrincipal.getAdmin().getId().longValue(), reason);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("관리자 경매 취소 중 알 수 없는 오류 발생. AuctionId: {}, AdminId: {}", auctionId, adminPrincipal.getAdmin().getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 취소 중 서버 내부 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> updateAuction(
            @PathVariable Integer auctionId,
            @Valid @RequestBody AuctionUpdateRequestDTO requestDto,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal
    ) {
        log.info("PUT /api/admin/auctions/{} - 관리자 경매 수정 요청. AdminId: {}, DTO: {}",
                auctionId, (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"), requestDto);

        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }

        if (!auctionId.equals(requestDto.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "경로의 경매 ID와 요청 본문의 ID가 일치하지 않습니다."));
        }

        try {
            AuctionResponseDTO updatedAuction = auctionService.updateAuction(requestDto, adminPrincipal.getAdmin().getId().longValue());
            return ResponseEntity.ok(ApiResponse.success(updatedAuction));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("관리자 경매 수정 중 알 수 없는 오류 발생. AuctionId: {}, AdminId: {}", auctionId, adminPrincipal.getAdmin().getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 수정 중 서버 내부 오류가 발생했습니다."));
        }
    }
} 
