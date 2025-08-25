package com.realive.repository.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.common.enums.AuctionStatus;

import java.util.List;
import java.util.Optional;

public interface AuctionRepositoryCustom {
    /**
     * 특정 사용자가 낙찰자로 등록된 완료된 경매를 조회합니다.
     *
     * @param userId      조회할 사용자 ID
     * @param auctionId   조회할 경매 ID
     * @return Optional.of(Auction) 또는 Optional.empty()
     */
    Optional<Auction> findWonAuctionByUserIdAndAuctionId(Long userId, Integer auctionId);
}
