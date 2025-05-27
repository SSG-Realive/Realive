package com.realive.repository.auction; // 실제 BidRepository의 패키지 경로

import com.realive.domain.auction.Bid; // 실제 Bid 엔티티 경로
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Query 어노테이션이 있다면 유지
import org.springframework.stereotype.Repository; // @Repository 어노테이션을 사용한다면 유지

import java.util.Optional;

@Repository // 사용하신다면 유지
public interface BidRepository extends JpaRepository<Bid, Integer> {

    // JpaRepository에서 기본 제공하지만 명시적으로 선언 가능
    Optional<Bid> findById(Integer id);

    // JPQL 쿼리 메소드 (기존 코드)
    @Query("select b.id, b.auctionId, b.customerId, b.bidPrice, " +
            "  b.bidTime " +
            " from Bid b ")
    Page<Object[]> list1(Pageable pageable);


    // --- 아래 메소드들을 추가하거나, BidServiceImpl에서 호출하는 메소드명과 일치하도록 수정 ---

    /**
     * 특정 경매의 모든 입찰 내역을 최신 입찰 시간 순으로 페이징하여 조회합니다.
     * BidServiceImpl의 getBidsForAuction 메소드에서 사용됩니다.
     * @param auctionId 경매 ID
     * @param pageable 페이징 및 정렬 정보
     * @return 입찰 내역 페이지
     */
    Page<Bid> findByAuctionIdOrderByBidTimeDesc(Integer auctionId, Pageable pageable);

    /**
     * 특정 고객의 모든 입찰 내역을 최신 입찰 시간 순으로 페이징하여 조회합니다.
     * BidServiceImpl의 getBidsByCustomer 메소드에서 사용됩니다.
     * @param customerId 고객 ID (Bid 엔티티의 customerId 타입에 맞춤 - 현재 Integer)
     * @param pageable 페이징 및 정렬 정보
     * @return 입찰 내역 페이지
     */
    Page<Bid> findByCustomerIdOrderByBidTimeDesc(Integer customerId, Pageable pageable);

    // 만약 BidServiceImpl에서 `findByAuctionId(Integer, Pageable)`와 `findByCustomerId(Integer, Pageable)`을
    // 직접 호출하고 있다면 (OrderByBidTimeDesc 없이), 해당 시그니처로 메소드를 정의해야 합니다.
    // 예: Page<Bid> findByAuctionId(Integer auctionId, Pageable pageable);
    // 예: Page<Bid> findByCustomerId(Integer customerId, Pageable pageable);
    // 이 경우, 정렬은 Pageable 객체에 Sort 정보를 담아서 전달해야 합니다.
}
