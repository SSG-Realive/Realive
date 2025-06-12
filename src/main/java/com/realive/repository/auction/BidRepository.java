package com.realive.repository.auction;

import com.realive.domain.auction.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {

    // JpaRepository에서 기본 제공하지만, 명시적으로 선언하여 가독성을 높일 수 있음
    Optional<Bid> findById(Integer id);

    /**
     * 특정 경매의 모든 입찰 내역을 최신 입찰 시간 순으로 페이징하여 조회합니다.
     * @param auctionId 경매 ID.
     * @param pageable 페이징 정보.
     * @return 입찰 내역 페이지.
     */
    Page<Bid> findByAuctionIdOrderByBidTimeDesc(Integer auctionId, Pageable pageable);

    /**
     * 특정 고객의 모든 입찰 내역을 최신 입찰 시간 순으로 페이징하여 조회합니다.
     * @param customerId 고객 ID.
     * @param pageable 페이징 정보.
     * @return 입찰 내역 페이지.
     */
    Page<Bid> findByCustomerIdOrderByBidTimeDesc(Integer customerId, Pageable pageable);
}
