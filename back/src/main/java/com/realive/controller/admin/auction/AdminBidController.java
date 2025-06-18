package com.realive.controller.admin.auction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.bid.BidResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.security.AdminPrincipal;
import com.realive.service.admin.auction.BidService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/bids")
public class AdminBidController {

    private final BidService bidService;

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getBidsForAuction(
            @PathVariable Integer auctionId,
            @PageableDefault(size = 20, sort = "bidTime", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        log.info("GET /api/admin/bids/auction/{} - 관리자가 특정 경매 입찰 내역 조회. AdminId: {}",
                auctionId, adminPrincipal.getAdmin().getId());
        try {
            Page<BidResponseDTO> bids = bidService.getBidsByAuction(auctionId, pageable);
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (Exception e) {
            log.error("관리자가 특정 경매(ID:{}) 입찰 내역 조회 중 알 수 없는 오류 발생", auctionId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 내역 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getCustomerBids(
            @PathVariable Long customerId,
            @PageableDefault(size = 20, sort = "bidTime", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        log.info("GET /api/admin/bids/customer/{} - 관리자가 고객 입찰 내역 조회. AdminId: {}", 
                customerId, adminPrincipal.getAdmin().getId());
        try {
            Page<BidResponseDTO> bids = bidService.getBidsByCustomer(customerId.intValue(), pageable);
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (Exception e) {
            log.error("관리자가 고객(ID:{}) 입찰 내역 조회 중 알 수 없는 오류 발생", customerId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 내역 조회 중 오류가 발생했습니다."));
        }
    }
} 