package com.realive.controller.auction;


import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.security.AdminPrincipal;   // 관리자 Principal (실제 경로로 수정)
// CustomerPrincipal을 직접 사용하지 않으므로 import는 필요 없을 수 있음
// import com.realive.security.CustomerPrincipal;
import com.realive.service.admin.auction.BidService; // 실제 BidService 경로
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
// import org.springframework.security.core.authority.SimpleGrantedAuthority; // 관리자 권한 확인용 (필요시)
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/bids") // 입찰 관련 기본 API 경로
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    /**
     * 고객이 경매에 입찰하는 API.
     * 인증된 고객의 ID를 SpEL을 통해 직접 주입받아 사용합니다.
     * @param requestDto 입찰 요청 정보 (경매 ID, 입찰 가격).
     * @param authenticatedCustomerId 현재 인증된 고객의 ID (SpEL을 통해 주입).
     * @return 생성된 입찰 정보 또는 에러 응답.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BidResponseDTO>> placeBid(
            @Valid @RequestBody BidRequestDTO requestDto,
            // 가정: Principal 객체에 'id' 필드 또는 'getId()' 메소드가 있고 Long 타입 고객 ID 반환
            @AuthenticationPrincipal(expression = "id") Long authenticatedCustomerId
    ) {
        log.info("POST /api/bids - 입찰 요청: {}, AuthenticatedCustomerId: {}", requestDto, authenticatedCustomerId);

        // customerId가 null이면 Spring Security 필터에서 먼저 걸러지거나,
        // @AuthenticationPrincipal이 null을 주입할 수 있으므로 방어적으로 확인
        if (authenticatedCustomerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "고객 로그인이 필요합니다."));
        }

        try {
            BidResponseDTO placedBid = bidService.placeBid(requestDto, authenticatedCustomerId);
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
            log.error("입찰 중 알 수 없는 오류 발생 - 요청: {}, CustomerId: {}", requestDto, authenticatedCustomerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 중 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 경매의 모든 입찰 내역을 조회하는 API.
     * (보안 정책에 따라 공개 또는 특정 권한 필요)
     */
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getBidsForAuction(
            @PathVariable Integer auctionId,
            @PageableDefault(size = 20, sort = "bidTime,desc") Pageable pageable
    ) {
        log.info("GET /api/bids/auction/{} - 특정 경매 입찰 내역 조회 요청", auctionId);
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

    /**
     * (고객용) 현재 인증된 고객 자신의 입찰 내역을 조회하는 API.
     * SpEL을 사용하여 Principal 객체에서 'id' (고객 ID)를 직접 주입받음.
     * @param pageable 페이징 및 정렬 정보.
     * @param myCustomerId 현재 인증된 고객의 ID (SpEL을 통해 주입).
     * @return 해당 고객의 입찰 내역 페이지 또는 에러 응답.
     */
    @GetMapping("/customer/my-bids") // "나의 입찰 내역"을 의미하는 경로
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getMyBids(
            @PageableDefault(size = 20, sort = "bidTime,desc") Pageable pageable,
            // 가정: Principal 객체에 'id' 필드 또는 'getId()' 메소드가 있고 Long 타입 고객 ID 반환
            @AuthenticationPrincipal(expression = "id") Long myCustomerId
    ) {
        log.info("GET /api/bids/customer/my-bids - 나의 입찰 내역 조회 요청. AuthenticatedCustomerId: {}", myCustomerId);

        if (myCustomerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "로그인이 필요합니다."));
        }

        try {
            Page<BidResponseDTO> bids = bidService.getBidsByCustomer(myCustomerId, pageable);
            if (bids.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success("나의 입찰 내역이 없습니다.", bids));
            }
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("나의 입찰 내역 조회 중 알 수 없는 오류 발생 - CustomerId: {}", myCustomerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "나의 입찰 내역 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * (관리자 전용) 특정 고객의 입찰 내역을 조회하는 API.
     * Spring Security 설정(@PreAuthorize 등)을 통해 관리자만 접근 가능하도록 제한해야 합니다.
     * @param customerId 조회할 고객의 ID.
     * @param pageable 페이징 및 정렬 정보.
     * @param adminPrincipal 현재 인증된 관리자 정보.
     * @return 해당 고객의 입찰 내역 페이지 또는 에러 응답.
     */
    @GetMapping("/admin/customer/{customerId}/bids") // 관리자용 API 경로
    public ResponseEntity<ApiResponse<Page<BidResponseDTO>>> getCustomerBidsForAdmin(
            @PathVariable Long customerId,
            @PageableDefault(size = 20, sort = "bidTime,desc") Pageable pageable,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal // 관리자는 AdminPrincipal 사용
    ) {
        log.info("GET /api/bids/admin/customer/{}/bids - 관리자가 고객 입찰 내역 조회. AdminId: {}",
                customerId, (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"));

        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        // Spring Security에서 이미 관리자 권한을 확인했거나,
        // 필요하다면 여기서 adminPrincipal.getAuthorities()로 추가 역할 검증 가능

        try {
            Page<BidResponseDTO> bids = bidService.getBidsByCustomer(customerId, pageable);
            if (bids.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success("해당 고객의 입찰 내역이 없습니다.", bids));
            }
            return ResponseEntity.ok(ApiResponse.success(bids));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("관리자가 고객(ID:{}) 입찰 내역 조회 중 알 수 없는 오류 발생", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "고객 입찰 내역 조회 중 오류가 발생했습니다."));
        }
    }
}
