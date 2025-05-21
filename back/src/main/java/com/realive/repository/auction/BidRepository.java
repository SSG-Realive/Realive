package com.realive.repository.auction;

import com.realive.domain.auction.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Integer> {

    Optional<Bid> findById(Integer id);

    @Query("select b.id, b.auctionId, b.customerId, b.bidPrice, " +
            "  b.bidTime " +
            " from Bid b ")
    Page<Object[]> list1(Pageable pageable);
}


