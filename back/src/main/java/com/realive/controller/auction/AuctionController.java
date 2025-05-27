package com.realive.controller.auction;

// import com.realive.domain.seller.Seller; // 이 시나리오에서는 Seller를 직접 받지 않을 수 있음
import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.security.AdminPrincipal; // 관리자 정보를 담는 Principal 클래스 (실제 경로로 수정)
import com.realive.service.admin.auction.AuctionService; // 패키지명 일치 확인 (service.admin.auction.AuctionService)
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
@RequestMapping("/api/admin/auctions") // 기본 경로를 관리자용으로 명확히 함
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    /**
     * (관리자에 의해) 새로운 경매를 등록합니다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> registerAuction(
            @Valid @RequestBody AuctionCreateRequestDTO requestDto,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal // 인증된 관리자 정보 주입
    ) {
        log.info("POST /api/admin/auctions - 관리자 경매 등록 요청 시작. DTO: {}, AdminId: {}",
                requestDto, (adminPrincipal != null ? adminPrincipal.getAdmin().getId() : "null"));

        if (adminPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        // Admin 엔티티의 ID가 Integer라고 가정하고 Long으로 변환. 실제 타입에 맞출 것.
        Long adminUserId = Long.valueOf(adminPrincipal.getAdmin().getId());

        try {
            // AuctionService.registerAuction은 이제 요청한 사용자의 ID (여기서는 관리자 ID)를 받음.
            // 서비스 내부에서 이 관리자가 경매를 생성할 권한이 있는지,
            // 또는 requestDto에 포함된 productId의 원래 판매자 정보를 활용하는지 등의 로직이 필요할 수 있음.
            AuctionResponseDTO registeredAuction = auctionService.registerAuction(requestDto, adminUserId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("경매가 성공적으로 등록되었습니다.", registeredAuction));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (AccessDeniedException e) { // 서비스에서 권한 검증 실패 시
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("관리자 경매 등록 중 알 수 없는 서버 내부 오류 발생. AdminId: {}", adminUserId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 등록 중 서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * (관리자가) 현재 진행 중인 경매 목록을 페이징하여 조회합니다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuctionResponseDTO>>> getActiveAuctions(
            @PageableDefault(size = 10, sort = "endTime,asc") Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal // 관리자만 접근 가능하도록
    ) {
        log.info("GET /api/admin/auctions - 관리자 진행 중 경매 목록 조회. AdminId: {}",
                (adminPrincipal != null ? adminPrincipal.getAdmin().getId() : "null"));
        if (adminPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
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

    /**
     * (관리자가) 특정 경매의 상세 정보를 조회합니다.
     */
    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> getAuctionDetails(
            @PathVariable Integer auctionId,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal // 관리자만 접근 가능하도록
    ) {
        log.info("GET /api/admin/auctions/{} - 관리자 경매 상세 정보 조회. AdminId: {}",
                auctionId, (adminPrincipal != null ? adminPrincipal.getAdmin().getId() : "null"));
        if (adminPrincipal == null) {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 상세 정보 조회 중 서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * (관리자가) 특정 판매자가 등록한 경매 목록을 페이징하여 조회합니다.
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<Page<AuctionResponseDTO>>> getAuctionsBySeller(
            @PathVariable Long sellerId, // 조회 대상 판매자 ID
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal // 이 API를 호출하는 주체는 관리자
    ) {
        log.info("GET /api/admin/auctions/seller/{} - 관리자가 특정 판매자 경매 목록 조회. AdminId: {}",
                sellerId, (adminPrincipal != null ? adminPrincipal.getAdmin().getId() : "null"));
        if (adminPrincipal == null) {
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

    /**
     * (관리자가) 특정 상품 ID에 대해 현재 진행 중인 경매를 조회합니다.
     */
    @GetMapping("/product/{productId}/current")
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> getCurrentAuctionForProduct(
            @PathVariable Integer productId,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal // 관리자만 접근 가능하도록
    ) {
        log.info("GET /api/admin/auctions/product/{}/current - 관리자가 특정 상품의 현재 경매 조회. AdminId: {}",
                productId, (adminPrincipal != null ? adminPrincipal.getAdmin().getId() : "null"));
        if (adminPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        try {
            Optional<AuctionResponseDTO> currentAuctionOpt = auctionService.getCurrentAuctionForProduct(productId);
            if (currentAuctionOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(currentAuctionOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.success("해당 상품에 대해 진행 중인 경매가 없습니다.", null));
            }
        } catch (Exception e) {
            log.error("관리자가 상품(ID:{})의 현재 경매 조회 중 오류 발생: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "상품의 현재 경매 조회 중 오류가 발생했습니다."));
        }
    }
}
