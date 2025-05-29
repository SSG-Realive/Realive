//package com.realive.service.admin.auction;
//
//import com.realive.dto.auction.AuctionCreateRequestDTO;
//import com.realive.dto.auction.AuctionResponseDTO;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.access.AccessDeniedException; // 명시적 import
//
//import java.util.NoSuchElementException;
//import java.util.Optional;
//
//public interface AuctionService {
//
//    /**
//     * (관리자에 의해) 새로운 경매를 등록합니다.
//     *
//     * @param requestDto 경매 생성 요청 정보를 담은 DTO.
//     * @param adminUserId 경매를 등록하는 관리자의 ID (인증된 관리자로부터 가져옴).
//     * @return 등록된 경매 정보를 담은 DTO.
//     * @throws NoSuchElementException 상품을 찾을 수 없거나, 요청된 상품 ID에 해당하는 AdminProduct가 없을 경우.
//     * @throws IllegalStateException 이미 해당 상품으로 진행 중인 경매가 있거나, 상품 상태(원본 Product 비활성 등)가 부적절할 경우.
//     * @throws AccessDeniedException 관리자가 해당 작업을 수행할 권한이 없을 경우 (예: 특정 관리자만 등록 가능 정책 위반).
//     * @throws IllegalArgumentException 요청 DTO의 유효성 검사 실패 또는 부적절한 파라미터 전달 시.
//     */
//    AuctionResponseDTO registerAuction(AuctionCreateRequestDTO requestDto, Long adminUserId);
//
//    /**
//     * (관리자가) 현재 진행 중인 또는 특정 조건의 경매 목록을 페이징하여 조회합니다.
//     *
//     * @param pageable 페이징 및 정렬 정보.
//     * @param categoryFilter (선택) 상품 카테고리 필터 문자열. (예: "ELECTRONICS", "BOOKS")
//     * @param statusFilter (선택) 경매 상태 필터 문자열. (예: "ON_AUCTION", "UPCOMING", "ENDED")
//     * @return 조건에 맞는 경매 목록의 Page 객체.
//     */
//    Page<AuctionResponseDTO> getActiveAuctions(Pageable pageable, String categoryFilter, String statusFilter);
//
//    /**
//     * (관리자가) 특정 경매의 상세 정보를 조회합니다.
//     *
//     * @param auctionId 조회할 경매의 ID.
//     * @return 경매 상세 정보를 담은 DTO.
//     * @throws NoSuchElementException 해당 ID의 경매를 찾을 수 없을 경우.
//     */
//    AuctionResponseDTO getAuctionDetails(Integer auctionId);
//
//    /**
//     * (관리자가) 특정 판매자가 등록한 경매 목록을 페이징하여 조회합니다.
//     * @param sellerId 조회 대상 판매자 ID.
//     * @param pageable 페이징 정보.
//     * @return 해당 판매자의 경매 목록 Page 객체.
//     * @throws NoSuchElementException 해당 sellerId의 판매자가 없거나 AdminProduct가 없을 경우 (서비스 구현에 따라).
//     */
//    Page<AuctionResponseDTO> getAuctionsBySeller(Long sellerId, Pageable pageable);
//
//    /**
//     * (관리자가) 특정 상품 ID에 대해 현재 진행 중인 경매를 조회합니다.
//     * (상품 하나당 하나의 활성 경매만 있다고 가정)
//     * @param productId 상품 ID (Auction 엔티티의 productId와 동일 타입).
//     * @return 진행 중인 경매 정보 Optional 객체. 없을 경우 Optional.empty() 반환.
//     */
//    Optional<AuctionResponseDTO> getCurrentAuctionForProduct(Integer productId);
//}
