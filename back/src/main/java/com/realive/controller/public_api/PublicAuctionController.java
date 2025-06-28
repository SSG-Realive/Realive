package com.realive.controller.public_api;

import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.service.admin.auction.AuctionService;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.service.admin.auction.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import com.realive.repository.auction.AuctionRepository;
import com.realive.util.TickSizeCalculator;
import com.realive.domain.auction.Auction;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/public/auctions")
@RequiredArgsConstructor
public class PublicAuctionController {

    private final AuctionService auctionService;
    private final BidService bidService;
    private final AuctionRepository auctionRepository;
    private final TickSizeCalculator tickSizeCalculator;

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

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getPublicAuctionBids(
            @PathVariable Integer auctionId,
            @PageableDefault(size = 10, sort = "bidTime", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/public/auctions/{}/bids - 공개 경매 입찰 내역 조회", auctionId);
        
        try {
            Page<BidResponseDTO> bids = bidService.getBidsByAuction(auctionId, pageable);
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (Exception e) {
            log.error("공개 경매 입찰 내역 조회 중 오류 발생 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 내역 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{auctionId}/tick-size")
    public ResponseEntity<ApiResponse<Integer>> getPublicTickSize(@PathVariable Integer auctionId) {
        log.info("GET /api/public/auctions/{}/tick-size - 공개 경매 입찰 단위 조회", auctionId);
        
        try {
            Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + auctionId));
            
            Integer tickSize = tickSizeCalculator.calculateTickSize(auction.getStartPrice());
            return ResponseEntity.ok(ApiResponse.success(tickSize));
        } catch (NoSuchElementException e) {
            log.error("공개 경매 정보 조회 실패 - AuctionId: {}, 에러: {}", auctionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("공개 경매 입찰 단위 조회 중 오류 발생 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 단위 조회 중 오류가 발생했습니다."));
        }
    }
} 