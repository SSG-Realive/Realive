package com.realive.service.admin.auction;

import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException; // 명시적 import

import java.util.NoSuchElementException;

public interface BidService {

    /**
     * 새로운 입찰을 등록합니다.
     *
     * @param requestDto 입찰 요청 정보를 담은 DTO (경매 ID, 입찰 가격).
     * @param customerId 입찰하는 고객의 ID (인증된 사용자로부터 가져옴, Long 타입).
     * @return 등록된 입찰 정보를 담은 DTO.
     * @throws NoSuchElementException 경매 또는 고객 정보를 찾을 수 없을 경우.
     * @throws IllegalStateException 경매가 진행 중이 아니거나(종료, 시작 전), 자기 상품 입찰 시도 등 입찰 조건 위반 시.
     * @throws IllegalArgumentException 입찰 가격이 유효하지 않을 경우 (현재 최고가보다 낮거나 같거나, 최소 입찰 단위를 만족하지 못할 경우 등).
     * @throws AccessDeniedException 고객 계정 상태(비활성, 입찰 제한)로 인해 입찰이 불가능한 경우.
     */
    BidResponseDTO placeBid(BidRequestDTO requestDto, Long customerId);

    /**
     * 특정 경매의 모든 입찰 내역을 페이징하여 조회합니다.
     *
     * @param auctionId 입찰 내역을 조회할 경매 ID.
     * @param pageable 페이징 및 정렬 정보.
     * @return 해당 경매의 입찰 내역 Page 객체.
     * @throws NoSuchElementException 해당 auctionId의 경매를 찾을 수 없을 경우.
     */
    Page<BidResponseDTO> getBidsForAuction(Integer auctionId, Pageable pageable);

    /**
     * 특정 고객의 모든 입찰 내역을 페이징하여 조회합니다.
     *
     * @param customerId 입찰 내역을 조회할 고객 ID (Long 타입).
     * @param pageable 페이징 및 정렬 정보.
     * @return 해당 고객의 입찰 내역 Page 객체.
     * @throws NoSuchElementException 해당 customerId의 고객 정보를 찾을 수 없을 경우.
     */
    Page<BidResponseDTO> getBidsByCustomer(Long customerId, Pageable pageable);

    Page<BidResponseDTO> getBidsByAuction(Long auctionId, Pageable pageable);
}
