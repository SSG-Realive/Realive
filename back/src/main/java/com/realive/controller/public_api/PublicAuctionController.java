package com.realive.controller.public_api;

import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.service.admin.auction.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

@Slf4j
@RestController
@RequestMapping("/api/public/auctions")
@RequiredArgsConstructor
public class PublicAuctionController {

    private final AuctionService auctionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuctionResponseDTO>>> getPublicAuctions(
            @PageableDefault(size = 10, sort = "endTime", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status
    ) {
        log.info("GET /api/public/auctions - 공개 경매 목록 조회. Category: {}, Status: {}", category, status);
        
        try {
            Page<AuctionResponseDTO> activeAuctions = auctionService.getActiveAuctions(pageable, category, status);
            if (activeAuctions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success("조건에 맞는 경매가 없습니다.", activeAuctions));
            }
            return ResponseEntity.ok(ApiResponse.success(activeAuctions));
        } catch (Exception e) {
            log.error("공개 경매 목록 조회 중 알 수 없는 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 목록 조회 중 서버 내부 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> getPublicAuctionDetail(@PathVariable Integer auctionId) {
        log.info("GET /api/public/auctions/{} - 공개 경매 상세 조회", auctionId);
        
        try {
            AuctionResponseDTO auction = auctionService.getAuctionDetails(auctionId);
            return ResponseEntity.ok(ApiResponse.success(auction));
        } catch (Exception e) {
            log.error("공개 경매 상세 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 상세 정보 조회 중 오류가 발생했습니다."));
        }
    }
} 