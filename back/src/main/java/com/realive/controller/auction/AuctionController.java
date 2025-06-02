package com.realive.controller.auction;

import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.security.AdminPrincipal; // 실제 AdminPrincipal 경로
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
import org.springframework.data.domain.Sort;

import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/admin/auctions") // 관리자 전용 경매 API 경로
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    /**
     * 관리자에 의한 새로운 경매 등록 API.
     * @param requestDto 경매 생성 정보 DTO.
     * @param adminPrincipal 인증된 관리자 정보.
     * @return 생성된 경매 정보 또는 에러 응답.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> registerAuction(
            @Valid @RequestBody AuctionCreateRequestDTO requestDto,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal // Spring Security를 통해 현재 인증된 관리자 주입
    ) {
        log.info("POST /api/admin/auctions - 관리자 경매 등록 요청. DTO: {}, AdminId: {}",
                requestDto, (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"));

        // 인증된 관리자인지 확인
        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        // Admin 엔티티의 ID 타입을 Long으로 변환하여 서비스에 전달 (실제 Admin 엔티티 ID 타입에 맞출 것)
        Long adminUserId = Long.valueOf(String.valueOf(adminPrincipal.getAdmin().getId()));

        try {
            // 서비스 계층에 경매 등록 요청
            AuctionResponseDTO registeredAuction = auctionService.registerAuction(requestDto, adminUserId);
            return ResponseEntity.status(HttpStatus.CREATED) // 성공 시 201 Created 응답
                    .body(ApiResponse.success("경매가 성공적으로 등록되었습니다.", registeredAuction));
        } catch (NoSuchElementException e) { // 관련된 상품/AdminProduct를 찾을 수 없을 때
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) { // 이미 등록된 경매, 상품 상태 부적절, 요청값 오류 등
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (AccessDeniedException e) { // 관리자 권한 부족 등 서비스 레벨에서 접근 거부 시
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) { // 기타 예외 발생 시
            log.error("관리자 경매 등록 중 알 수 없는 서버 내부 오류 발생. AdminId: {}, DTO: {}", adminUserId, requestDto, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 등록 중 서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 관리자가 조건에 맞는 경매 목록을 조회하는 API.
     * 페이징, 정렬, 카테고리 필터, 상태 필터를 지원.
     * @param pageable 페이징 및 정렬 정보 (예: /api/admin/auctions?page=0&size=5&sort=endTime,desc)
     * @param category (선택) 상품 카테고리 필터 문자열.
     * @param status (선택) 경매 상태 필터 문자열 (예: "ON_AUCTION", "UPCOMING", "ENDED").
     * @param adminPrincipal 인증된 관리자 정보.
     * @return 경매 목록 또는 에러 응답.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuctionResponseDTO>>> getActiveAuctions(
            @PageableDefault(size = 10, sort = "endTime", direction = Sort.Direction.ASC) Pageable pageable, // 기본 페이징: 10개씩, 마감시간 오름차순
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal
    ) {
        log.info("GET /api/admin/auctions - 관리자 경매 목록 조회. AdminId: {}, Category: {}, Status: {}",
                (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"), category, status);
        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        try {
            Page<AuctionResponseDTO> activeAuctions = auctionService.getActiveAuctions(pageable, category, status);
            if (activeAuctions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK) // 결과가 없어도 200 OK
                        .body(ApiResponse.success("조건에 맞는 경매가 없습니다.", activeAuctions));
            }
            return ResponseEntity.ok(ApiResponse.success(activeAuctions));
        } catch (Exception e) {
            log.error("관리자 경매 목록 조회 중 알 수 없는 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 목록 조회 중 서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 관리자가 특정 경매의 상세 정보를 조회하는 API.
     * @param auctionId 조회할 경매의 ID.
     * @param adminPrincipal 인증된 관리자 정보.
     * @return 경매 상세 정보 또는 에러 응답.
     */
    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> getAuctionDetails(
            @PathVariable Integer auctionId,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal
    ) {
        log.info("GET /api/admin/auctions/{} - 관리자 경매 상세 정보 조회. AdminId: {}",
                auctionId, (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"));
        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        try {
            AuctionResponseDTO auctionDetails = auctionService.getAuctionDetails(auctionId);
            return ResponseEntity.ok(ApiResponse.success(auctionDetails));
        } catch (NoSuchElementException e) { // 해당 ID의 경매가 없을 때
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("관리자 경매 상세 정보(AuctionId:{}) 조회 중 알 수 없는 오류 발생", auctionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "경매 상세 정보 조회 중 서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 관리자가 특정 판매자가 등록한 경매 목록을 조회하는 API.
     * @param sellerId 조회할 판매자의 ID.
     * @param pageable 페이징 및 정렬 정보.
     * @param adminPrincipal 인증된 관리자 정보.
     * @return 해당 판매자의 경매 목록 또는 에러 응답.
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<Page<AuctionResponseDTO>>> getAuctionsBySeller(
            @PathVariable Long sellerId,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable, // 기본: 등록시간 내림차순
            @AuthenticationPrincipal AdminPrincipal adminPrincipal
    ) {
        log.info("GET /api/admin/auctions/seller/{} - 관리자가 특정 판매자 경매 목록 조회. AdminId: {}",
                sellerId, (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"));
        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
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
     * 관리자가 특정 상품 ID에 대해 현재 진행 중인 경매를 조회하는 API.
     * @param productId 조회할 상품의 ID.
     * @param adminPrincipal 인증된 관리자 정보.
     * @return 진행 중인 경매 정보 또는 에러 응답.
     */
    @GetMapping("/product/{productId}/current")
    public ResponseEntity<ApiResponse<AuctionResponseDTO>> getCurrentAuctionForProduct(
            @PathVariable Integer productId,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal
    ) {
        log.info("GET /api/admin/auctions/product/{}/current - 관리자가 특정 상품의 현재 경매 조회. AdminId: {}",
                productId, (adminPrincipal != null && adminPrincipal.getAdmin() != null ? adminPrincipal.getAdmin().getId() : "null"));
        if (adminPrincipal == null || adminPrincipal.getAdmin() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
        }
        try {
            Optional<AuctionResponseDTO> currentAuctionOpt = auctionService.getCurrentAuctionForProduct(productId);
            if (currentAuctionOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(currentAuctionOpt.get()));
            } else {
                // 진행 중인 경매가 없으면 404 Not Found 응답
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(),"해당 상품에 대해 진행 중인 경매가 없습니다."));
            }
        } catch (Exception e) {
            log.error("관리자가 상품(ID:{})의 현재 경매 조회 중 알 수 없는 오류 발생: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "상품의 현재 경매 조회 중 오류가 발생했습니다."));
        }
    }
}
