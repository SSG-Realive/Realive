package com.realive.service.auction;

import com.realive.dto.auction.AuctionWinnerResponseDTO;

public interface AuctionService2 {
    /**
     * 특정 고객이 낙찰자로 등록된 경매의 상세 정보를 조회합니다.
     *
     * @param customerId  조회할 고객 ID
     * @param auctionId   조회할 경매 ID
     * @return 낙찰자용 상세 응답 DTO
     */
    AuctionWinnerResponseDTO getWinnerAuctionDetail(Long customerId, Integer auctionId);
}
