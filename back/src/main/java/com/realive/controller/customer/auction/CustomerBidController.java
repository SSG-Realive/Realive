package com.realive.controller.customer.auction;

import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.dto.customer.member.MemberLoginDTO;
import com.realive.service.admin.auction.BidService;
import com.realive.service.admin.auction.AuctionService;
import com.realive.util.TickSizeCalculator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/customer/bids")
@RequiredArgsConstructor
public class CustomerBidController {

    private final BidService bidService;
    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<ApiResponse<BidResponseDTO>> placeBid(
            @Valid @RequestBody BidRequestDTO requestDto,
            @AuthenticationPrincipal MemberLoginDTO userDetails
    ) {
        log.info("POST /api/customer/bids - 입찰 요청: {}, CustomerId: {}", requestDto, userDetails.getId());

        if (userDetails.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "고객 로그인이 필요합니다."));
        }

        try {
            BidResponseDTO placedBid = bidService.placeBid(requestDto, userDetails.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("입찰이 성공적으로 등록되었습니다.", placedBid));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("입찰 중 알 수 없는 오류 발생 - 요청: {}, CustomerId: {}", requestDto, userDetails.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getBidsForAuction(
            @PathVariable Integer auctionId,
            @PageableDefault(size = 20, sort = "bidTime,desc") Pageable pageable
    ) {
        log.info("GET /api/customer/bids/auction/{} - 특정 경매 입찰 내역 조회 요청", auctionId);
        try {
            Page<BidResponseDTO> bids = bidService.getBidsForAuction(auctionId, pageable);
            if (bids.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success("해당 경매에 대한 입찰 내역이 없습니다.", bids));
            }
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (NoSuchElementException e) {
            log.warn("경매(ID:{}) 입찰 내역 조회 실패 - 존재하지 않는 경매: {}", auctionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("특정 경매(ID:{}) 입찰 내역 조회 중 알 수 없는 오류 발생", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 내역 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<Page<BidResponseDTO>> getBidsByAuction(
            @PathVariable Long auctionId,
            @PageableDefault(size = 20, sort = "bidTime", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/customer/bids/{} - 경매별 입찰 내역 조회 요청", auctionId);
        Page<BidResponseDTO> bids = bidService.getBidsByAuction(auctionId, pageable);
        return ResponseEntity.ok(bids);
    }

    @GetMapping("/my-bids")
    public ResponseEntity<Page<BidResponseDTO>> getMyBids(
            @AuthenticationPrincipal MemberLoginDTO userDetails,
            @PageableDefault(size = 20, sort = "bidTime", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/customer/bids/my-bids - 나의 입찰 내역 조회 요청. CustomerId: {}", userDetails.getId());
        try {
            Page<BidResponseDTO> bids = bidService.getBidsByCustomer(userDetails.getId(), pageable);
            return ResponseEntity.ok(bids);
        } catch (Exception e) {
            log.error("나의 입찰 내역 조회 중 알 수 없는 오류 발생 - CustomerId: {}", userDetails.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/auction/{auctionId}/tick-size")
    public ResponseEntity<ApiResponse<Integer>> getTickSize(@PathVariable Integer auctionId) {
        log.info("GET /api/customer/bids/auction/{}/tick-size - 입찰 단위 정보 조회", auctionId);
        try {
            int startPrice = auctionService.getAuctionDetails(auctionId).getStartPrice();
            int tickSize = TickSizeCalculator.calculateTickSize(startPrice);
            return ResponseEntity.ok(ApiResponse.success(tickSize));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("입찰 단위 정보 조회 중 오류 발생 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 단위 정보 조회 중 오류가 발생했습니다."));
        }
    }
} 