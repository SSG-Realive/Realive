package com.realive.repository.auction;

import com.realive.domain.auction.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Integer> {

    // 특정 상품 ID로 모든 경매 조회
    List<Auction> findByProductId(Integer productId);

    // 특정 시작 시간 이후의 모든 경매 조회
    List<Auction> findByStartTimeAfter(LocalDateTime startTime);

    // 특정 종료 시간 이전의 모든 경매 조회
    List<Auction> findByEndTimeBefore(LocalDateTime endTime);

    // 종료되지 않은 모든 경매 조회
    List<Auction> findByIsClosedFalse();

    // 종료된 모든 경매 조회
    List<Auction> findByIsClosedTrue();

    // 특정 상품 ID에 대해 종료되지 않은 경매 조회 (Optional 반환 - 없을 수도 있음)
    Optional<Auction> findByProductIdAndIsClosedFalse(Integer productId);

    // 특정 현재 가격 이상인 모든 경매 조회
    List<Auction> findByCurrentPriceGreaterThanEqual(Integer price);

}
