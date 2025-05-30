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
     * Pageable 객체에 포함된 정렬 정보는 이 메소드명의 OrderBy절에 의해 덮어쓰여질 수 있습니다.
     * 명시적인 정렬이 필요 없다면 메소드명에서 OrderBy를 제거하고 Pageable의 Sort를 활용하세요.
     * @param auctionId 경매 ID.
     * @param pageable 페이징 정보.
     * @return 입찰 내역 페이지.
     */
    Page<Bid> findByAuctionIdOrderByBidTimeDesc(Integer auctionId, Pageable pageable);

    /**
     * 특정 고객의 모든 입찰 내역을 최신 입찰 시간 순으로 페이징하여 조회합니다.
     * Pageable 객체에 포함된 정렬 정보는 이 메소드명의 OrderBy절에 의해 덮어쓰여질 수 있습니다.
     * 명시적인 정렬이 필요 없다면 메소드명에서 OrderBy를 제거하고 Pageable의 Sort를 활용하세요.
     * @param customerId 고객 ID (Bid 엔티티의 customerId 타입과 일치 - 현재 Integer).
     * @param pageable 페이징 정보.
     * @return 입찰 내역 페이지.
     */
    Page<Bid> findByCustomerIdOrderByBidTimeDesc(Integer customerId, Pageable pageable);

    // 만약 Pageable 객체의 Sort 정보를 우선적으로 사용하고 싶다면, 아래와 같이 메소드명에서 OrderBy절을 제거합니다.
    // 이 경우, 서비스 계층에서 PageRequest.of(page, size, Sort.by(Direction.DESC, "bidTime")) 형태로 Sort를 명시해야 합니다.
    // Page<Bid> findByAuctionId(Integer auctionId, Pageable pageable);
    // Page<Bid> findByCustomerId(Integer customerId, Pageable pageable);
}
