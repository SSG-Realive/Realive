package com.realive.repository.auction;

import com.realive.domain.auction.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Integer> {

    List<Auction> findByProductId(Integer productId);

    List<Auction> findByStartTimeAfter(LocalDateTime startTime);

    List<Auction> findByEndTimeBefore(LocalDateTime endTime);

    List<Auction> findByIsClosedFalse();

    List<Auction> findByIsClosedTrue();

    Optional<Auction> findByProductIdAndIsClosedFalse(Integer productId);

    List<Auction> findByCurrentPriceGreaterThanEqual(Integer price);

} //일단 g로 넣었습니다.
