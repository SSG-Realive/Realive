package com.realive.controller.customer.auction;

import com.realive.domain.auction.Auction;
import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.dto.customer.member.MemberLoginDTO;
import com.realive.repository.auction.AuctionRepository;
import com.realive.service.admin.auction.BidService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/customer/bids")
@RequiredArgsConstructor
@Slf4j
public class CustomerBidController {

    private final BidService bidService;
    private final AuctionRepository auctionRepository;
    private final TickSizeCalculator tickSizeCalculator;

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

    @PostMapping
    public ResponseEntity<ApiResponse<BidResponseDTO>> placeBid(
            @Valid @RequestBody BidRequestDTO requestDto) {
        try {
            Long customerId = getAuthenticatedCustomerId();
            
            log.info("POST /api/customer/bids - 입찰 요청 데이터: auctionId={}, bidPrice={}, CustomerId={}", 
                requestDto.getAuctionId(), requestDto.getBidPrice(), customerId);
            
            if (requestDto.getAuctionId() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "경매 ID는 필수입니다."));
            }
            
            if (requestDto.getBidPrice() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "입찰 가격은 필수입니다."));
            }
            
            BidResponseDTO placedBid = bidService.placeBid(
                requestDto.getAuctionId(),
                customerId.intValue(),
                requestDto
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("입찰이 성공적으로 등록되었습니다.", placedBid));
        } catch (AccessDeniedException e) {
            log.error("입찰 권한 없음 - 요청: {}, CustomerId: {}", requestDto, 
                SecurityContextHolder.getContext().getAuthentication() != null ? 
                ((MemberLoginDTO)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId() : null, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("입찰 유효성 검증 실패 - 요청: {}, CustomerId: {}, 에러: {}", requestDto, 
                SecurityContextHolder.getContext().getAuthentication() != null ? 
                ((MemberLoginDTO)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId() : null, 
                e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("입찰 상태 오류 - 요청: {}, CustomerId: {}, 에러: {}", requestDto, 
                SecurityContextHolder.getContext().getAuthentication() != null ? 
                ((MemberLoginDTO)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId() : null, 
                e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (NoSuchElementException e) {
            log.error("입찰 대상 조회 실패 - 요청: {}, CustomerId: {}, 에러: {}", requestDto, 
                SecurityContextHolder.getContext().getAuthentication() != null ? 
                ((MemberLoginDTO)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId() : null, 
                e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("입찰 중 알 수 없는 오류 발생 - 요청: {}, CustomerId: {}", requestDto, 
                SecurityContextHolder.getContext().getAuthentication() != null ? 
                ((MemberLoginDTO)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId() : null, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/my-bids")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getMyBids(
            @PageableDefault(size = 20, sort = "bidTime", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Long customerId = getAuthenticatedCustomerId();
            
            log.info("GET /api/customer/bids/my-bids - 나의 입찰 내역 조회 요청. CustomerId: {}", customerId);
            Page<BidResponseDTO> bids = bidService.getBidsByCustomer(customerId.intValue(), pageable);
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (AccessDeniedException e) {
            log.error("입찰 내역 조회 권한 없음 - CustomerId: {}", 
                SecurityContextHolder.getContext().getAuthentication() != null ? 
                ((MemberLoginDTO)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId() : null, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("나의 입찰 내역 조회 중 알 수 없는 오류 발생 - CustomerId: {}", 
                SecurityContextHolder.getContext().getAuthentication() != null ? 
                ((MemberLoginDTO)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId() : null, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 내역 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/auction/{auctionId}/tick-size")
    public ResponseEntity<ApiResponse<Integer>> getTickSize(@PathVariable Integer auctionId) {
        try {
            log.info("GET /api/customer/bids/auction/{}/tick-size - 입찰 단위 조회 요청", auctionId);
            
            Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + auctionId));
            
            Integer tickSize = tickSizeCalculator.calculateTickSize(auction.getStartPrice());
            return ResponseEntity.ok(ApiResponse.success(tickSize));
        } catch (NoSuchElementException e) {
            log.error("경매 정보 조회 실패 - AuctionId: {}, 에러: {}", auctionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("입찰 단위 조회 중 오류 발생 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 단위 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getBidsByAuction(
            @PathVariable Integer auctionId,
            @PageableDefault(size = 20, sort = "bidTime", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            log.info("GET /api/customer/bids/{} - 경매 입찰 내역 조회 요청", auctionId);
            Page<BidResponseDTO> bids = bidService.getBidsByAuction(auctionId, pageable);
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (NoSuchElementException e) {
            log.error("경매 입찰 내역 조회 실패 - AuctionId: {}, 에러: {}", auctionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("경매 입찰 내역 조회 중 오류 발생 - AuctionId: {}", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 내역 조회 중 오류가 발생했습니다."));
        }
    }
} 