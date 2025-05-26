package com.realive.service.admin.auction;

import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.NoSuchElementException;

public interface AuctionService {

    /**
     * 새로운 경매를 등록합니다.
     *
     * @param requestDto 경매 생성 요청 정보를 담은 DTO
     * @param sellerUserId 경매를 등록하는 판매자의 ID (인증된 사용자로부터 가져옴)
     * @return 등록된 경매 정보를 담은 DTO
     * @throws NoSuchElementException 상품을 찾을 수 없거나, 판매자 정보가 일치하지 않을 경우
     * @throws IllegalStateException 이미 해당 상품으로 진행 중인 경매가 있거나, 상품 상태가 부적절할 경우
     */
    AuctionResponseDTO registerAuction(AuctionCreateRequestDTO requestDto, Long sellerUserId);

    /**
     * 현재 진행 중인 경매 목록을 페이징하여 조회합니다.
     *
     * @param pageable 페이징 및 정렬 정보
     * @param categoryFilter (선택) 상품 카테고리 필터
     * @param statusFilter (선택) 경매 상태 필터 (예: "ON_AUCTION", "CLOSING_SOON" 등)
     * @return 진행 중인 경매 목록의 Page 객체
     */
    Page<AuctionResponseDTO> getActiveAuctions(Pageable pageable, String categoryFilter, String statusFilter);

    /**
     * 특정 경매의 상세 정보를 조회합니다.
     *
     * @param auctionId 조회할 경매의 ID
     * @return 경매 상세 정보를 담은 DTO
     * @throws NoSuchElementException 해당 ID의 경매를 찾을 수 없을 경우
     */
    AuctionResponseDTO getAuctionDetails(Integer auctionId);

    // TODO: 필요에 따라 추가적인 서비스 메소드 정의
    // 예: 특정 판매자가 등록한 경매 목록 조회, 특정 상품에 대한 현재 진행 중 경매 조회 등
}
