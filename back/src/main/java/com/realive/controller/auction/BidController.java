package com.realive.controller.auction;

import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.security.CustomerPrincipal; // 실제 CustomerPrincipal 경로
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 관리자 권한 확인용
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/bids") // 입찰 관련 API 경로
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    /**
     * 고객이 경매에 입찰하는 API.
     * @param requestDto 입찰 요청 정보 (경매 ID, 입찰 가격).
     * @param customerPrincipal 인증된 고객 정보.
     * @return 생성된 입찰 정보 또는 에러 응답.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BidResponseDTO>> placeBid(
            @Valid @RequestBody BidRequestDTO requestDto,
            @AuthenticationPrincipal CustomerPrincipal customerPrincipal // Spring Security를 통해 현재 인증된 고객 주입
    ) {
        log.info("POST /api/bids - 입찰 요청: {}, CustomerId: {}",
                requestDto, (customerPrincipal != null && customerPrincipal.getCustomer() != null ? customerPrincipal.getCustomer().getId() : "null"));

        // 인증된 고객인지 확인
        if (customerPrincipal == null || customerPrincipal.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "고객 로그인이 필요합니다."));
        }
        Long authenticatedCustomerId = customerPrincipal.getCustomer().getId(); // Customer 엔티티의 ID가 Long이라고 가정

        try {
            // 서비스 계층에 입찰 요청 (인증된 고객 ID 전달)
            BidResponseDTO placedBid = bidService.placeBid(requestDto, authenticatedCustomerId);
            return ResponseEntity.status(HttpStatus.CREATED) // 성공 시 201 Created 응답
                    .body(ApiResponse.success("입찰이 성공적으로 등록되었습니다.", placedBid));
        } catch (NoSuchElementException e) { // 관련된 경매/고객 정보를 찾을 수 없을 때
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) { // 입찰가 유효성, 경매 상태, 고객 상태 등 문제
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (AccessDeniedException e) { // 고객 계정 상태(비활성, 제한) 등으로 인한 입찰 불가 시
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) { // 기타 예외 발생 시
            log.error("입찰 중 알 수 없는 오류 발생 - 요청: {}, CustomerId: {}", requestDto, authenticatedCustomerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 중 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 경매의 모든 입찰 내역을 조회하는 API. (공개 또는 특정 권한 필요)
     * @param auctionId 조회할 경매의 ID.
     * @param pageable 페이징 및 정렬 정보.
     * @return 해당 경매의 입찰 내역 페이지 또는 에러 응답.
     */
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getBidsForAuction(
            @PathVariable Integer auctionId,
            @PageableDefault(size = 20, sort = "bidTime,desc") Pageable pageable // 기본: 입찰시간 내림차순
            // 이 API는 공개할지, 아니면 특정 역할(예: 관리자, 경매 참여자)만 접근 가능하게 할지 보안 정책 필요
    ) {
        log.info("GET /api/bids/auction/{} - 특정 경매 입찰 내역 조회 요청", auctionId);
        try {
            Page<BidResponseDTO> bids = bidService.getBidsForAuction(auctionId, pageable);
            if (bids.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success("해당 경매에 대한 입찰 내역이 없습니다.", bids));
            }
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (NoSuchElementException e) { // 서비스에서 해당 경매를 찾지 못했을 때
            log.warn("경매(ID:{}) 입찰 내역 조회 실패 - 존재하지 않는 경매: {}", auctionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("특정 경매(ID:{}) 입찰 내역 조회 중 알 수 없는 오류 발생", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 내역 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 고객의 모든 입찰 내역을 조회하는 API. (본인 또는 관리자만 가능)
     * @param customerId 조회할 고객의 ID.
     * @param pageable 페이징 및 정렬 정보.
     * @param authenticatedUser 현재 인증된 사용자 정보 (고객 또는 관리자).
     * @return 해당 고객의 입찰 내역 페이지 또는 에러 응답.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getBidsByCustomer(
            @PathVariable Long customerId, // 조회 대상 고객 ID
            @PageableDefault(size = 20, sort = "bidTime,desc") Pageable pageable,
            @AuthenticationPrincipal CustomerPrincipal authenticatedUser // 인증된 사용자 (CustomerPrincipal 가정)
    ) {
        log.info("GET /api/bids/customer/{} - 특정 고객 입찰 내역 조회 요청. AuthenticatedUserId: {}",
                customerId, (authenticatedUser != null && authenticatedUser.getCustomer() != null ? authenticatedUser.getCustomer().getId() : "null"));

        // 인증된 사용자인지 확인
        if (authenticatedUser == null || authenticatedUser.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "로그인이 필요합니다."));
        }
        Long authenticatedUserId = authenticatedUser.getCustomer().getId();

        // 권한 검증: 요청한 customerId가 로그인한 사용자의 ID와 일치하거나, 로그인한 사용자가 관리자인 경우
        // CustomerPrincipal에 isAdmin() 또는 getAuthorities()로 역할을 확인하는 메소드가 있다고 가정
        boolean isAdmin = authenticatedUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")); // 예시 권한 확인

        if (!authenticatedUserId.equals(customerId) && !isAdmin) {
            log.warn("권한 없음 - CustomerId {}의 입찰 내역 조회 시도. (Authenticated: {}, IsAdmin: {})",
                    customerId, authenticatedUserId, isAdmin);
            // AccessDeniedException을 던지거나, 직접 403 응답 반환
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "자신의 입찰 내역만 조회할 수 있습니다."));
        }

        try {
            Page<BidResponseDTO> bids = bidService.getBidsByCustomer(customerId, pageable);
            if (bids.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success("해당 고객의 입찰 내역이 없습니다.", bids));
            }
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (NoSuchElementException e) { // 서비스에서 해당 고객을 찾지 못했을 때
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("특정 고객(ID:{}) 입찰 내역 조회 중 알 수 없는 오류 발생", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 내역 조회 중 오류가 발생했습니다."));
        }
    }
}
