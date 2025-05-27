package com.realive.controller.auction;

import com.realive.domain.seller.Seller; // Seller 엔티티 import (필요시)
import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.dto.common.ApiResponse;
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
import java.util.Optional; // Optional import 추가

@Slf4j
@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    // --- 기존 엔드포인트 (registerAuction, getActiveAuctions, getAuctionDetails) ---
    // (이전 답변의 코드를 그대로 사용하시면 됩니다. 여기서는 생략합니다.)
    // ... (POST /api/auctions - registerAuction 메소드) ...
    // ... (GET /api/auctions - getActiveAuctions 메소드) ...
    // ... (GET /api/auctions/{auctionId} - getAuctionDetails 메소드) ...

    @PostMapping
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> registerAuction(
            @Valid @RequestBody AuctionCreateRequestDTO requestDto,
            @AuthenticationPrincipal Seller seller
    ) {
        log.info("POST /api/auctions - 경매 등록 요청 시작. DTO: {}, SellerId: {}", requestDto, (seller != null ? seller.getId() : "null"));
        if (seller == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "판매자 로그인이 필요합니다."));
        }
        Long sellerId = seller.getId();
        try {
            AuctionResponseDTO registeredAuction = auctionService.registerAuction(requestDto, sellerId);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 등록 중 서버 내부 오류가 발생했습니다."));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuctionResponseDTO>>> getActiveAuctions(
            @PageableDefault(size = 10, sort = "endTime,asc") Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status
    ) {
        log.info("GET /api/auctions - 진행 중인 경매 목록 조회 요청. Pageable: {}, Category: {}, Status: {}", pageable, category, status);
        try {
            Page<AuctionResponseDTO> activeAuctions = auctionService.getActiveAuctions(pageable, category, status);
            if (activeAuctions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success("진행 중인 경매가 없습니다.", activeAuctions));
            }
            return ResponseEntity.ok(ApiResponse.success(activeAuctions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 목록 조회 중 서버 내부 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> getAuctionDetails(@PathVariable Integer auctionId) {
        log.info("GET /api/auctions/{} - 경매 상세 정보 조회 요청", auctionId);
        try {
            AuctionResponseDTO auctionDetails = auctionService.getAuctionDetails(auctionId);
            return ResponseEntity.ok(ApiResponse.success(auctionDetails));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 상세 정보 조회 중 서버 내부 오류가 발생했습니다."));
        }
    }


    // --- 새로 추가된 API 엔드포인트 ---

    /**
     * 특정 판매자가 등록한 경매 목록을 페이징하여 조회합니다.
     * 엔드포인트: GET /api/auctions/seller/{sellerId}
     * (보안: 실제로는 로그인한 판매자 본인의 경매만 보거나, 관리자가 특정 판매자의 경매를 보는 시나리오를 고려해야 함)
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<Page<AuctionResponseDTO>>> getAuctionsBySeller(
            @PathVariable Long sellerId,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable
            /* @AuthenticationPrincipal UserDetailsImpl userDetails */ // 필요시 현재 사용자 정보 받아 권한 체크
    ) {
        log.info("GET /api/auctions/seller/{} - 특정 판매자 경매 목록 조회 요청", sellerId);
        // TODO: 보안 - 요청한 sellerId가 현재 로그인한 사용자와 일치하는지, 또는 관리자 권한인지 확인 필요
        // 예를 들어, @AuthenticationPrincipal Seller currentSeller 와 비교하거나, currentSeller.getRole() 확인
        try {
            Page<AuctionResponseDTO> sellerAuctions = auctionService.getAuctionsBySeller(sellerId, pageable);
            if (sellerAuctions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success("해당 판매자가 등록한 경매가 없습니다.", sellerAuctions));
            }
            return ResponseEntity.ok(ApiResponse.success(sellerAuctions));
        } catch (Exception e) {
            log.error("판매자(ID:{}) 경매 목록 조회 중 알 수 없는 오류 발생: {}", sellerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "판매자 경매 목록 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 상품 ID에 대해 현재 진행 중인 경매를 조회합니다.
     * 엔드포인트: GET /api/auctions/product/{productId}/current
     */
    @GetMapping("/product/{productId}/current")
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> getCurrentAuctionForProduct(
            @PathVariable Integer productId
    ) {
        log.info("GET /api/auctions/product/{}/current - 특정 상품의 현재 진행 중 경매 조회 요청", productId);
        try {
            Optional<AuctionResponseDTO> currentAuctionOpt = auctionService.getCurrentAuctionForProduct(productId);
            if (currentAuctionOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(currentAuctionOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND) // 진행 중인 경매가 없으면 404
                        .body(ApiResponse.success("해당 상품에 대해 진행 중인 경매가 없습니다.", null)); // 또는 error로 처리
            }
        } catch (Exception e) {
            log.error("상품(ID:{})의 현재 경매 조회 중 알 수 없는 오류 발생: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "상품의 현재 경매 조회 중 오류가 발생했습니다."));
        }
    }
}
