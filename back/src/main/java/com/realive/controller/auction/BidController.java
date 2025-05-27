package com.realive.controller.auction;

import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.service.admin.auction.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import com.realive.security.UserDetailsImpl;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<ApiResponse<BidResponseDTO>> placeBid(
            @Valid @RequestBody BidRequestDTO requestDto
            /* @AuthenticationPrincipal UserDetailsImpl userDetails */
    ) {
        log.info("POST /api/bids - 입찰 요청: {}", requestDto);
        Long mockAuthenticatedCustomerId = 2L;

        try {
            BidResponseDTO placedBid = bidService.placeBid(requestDto, mockAuthenticatedCustomerId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("입찰이 성공적으로 등록되었습니다.", placedBid));
        } catch (NoSuchElementException e) {
            log.warn("입찰 실패 - 관련 리소스 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("입찰 실패 - 잘못된 요청 또는 상태: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("입찰 실패 - 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("입찰 중 알 수 없는 오류 발생 - 요청: {}", requestDto, e); // 로깅 시 예외 객체 e도 함께 전달
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 중 오류가 발생했습니다.")); // 수정됨
        }
    }

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getBidsForAuction(
            @PathVariable Integer auctionId,
            @PageableDefault(size = 20, sort = "bidTime,desc") Pageable pageable
    ) {
        log.info("GET /api/bids/auction/{} - 특정 경매 입찰 내역 조회 요청", auctionId);
        try {
            Page<BidResponseDTO> bids = bidService.getBidsForAuction(auctionId, pageable);
            if (bids.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK) // 204 No Content 대신 200 OK와 빈 데이터로 응답할 수도 있음
                        .body(ApiResponse.success("해당 경매에 대한 입찰 내역이 없습니다.", bids));
            }
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (NoSuchElementException e) {
            log.warn("경매(ID:{}) 입찰 내역 조회 실패: {}", auctionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("특정 경매 입찰 내역 조회 중 알 수 없는 오류 발생 - AuctionId {}: {}", auctionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 내역 조회 중 오류가 발생했습니다.")); // 수정됨
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getBidsByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 20, sort = "bidTime,desc") Pageable pageable
            /* @AuthenticationPrincipal UserDetailsImpl userDetails */
    ) {
        log.info("GET /api/bids/customer/{} - 특정 고객 입찰 내역 조회 요청", customerId);
        try {
            Page<BidResponseDTO> bids = bidService.getBidsByCustomer(customerId, pageable);
            if (bids.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK) // 204 No Content 대신 200 OK와 빈 데이터로 응답할 수도 있음
                        .body(ApiResponse.success("해당 고객의 입찰 내역이 없습니다.", bids));
            }
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (Exception e) {
            log.error("특정 고객 입찰 내역 조회 중 알 수 없는 오류 발생 - CustomerId {}: {}", customerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 내역 조회 중 오류가 발생했습니다.")); // 수정됨
        }
    }
}
