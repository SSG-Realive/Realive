package com.realive.repository.auction;

import com.realive.domain.auction.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // JpaSpecificationExecutor를 사용한다면 주석 해제
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Integer> /* , JpaSpecificationExecutor<Auction> */ { // JpaSpecificationExecutor를 사용한다면 주석 해제

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

    // 현재 진행 중인 경매 목록 조회 (종료되지 않았고, 종료 시간이 현재 이후)
    Page<Auction> findByIsClosedFalseAndEndTimeAfter(LocalDateTime currentTime, Pageable pageable);

    // --- 아래 메소드가 추가된 부분입니다 ---
    /**
     * 여러 Product ID에 해당하는 Auction 목록을 페이징하여 조회합니다.
     * AuctionServiceImpl의 getAuctionsBySeller 메소드에서 사용됩니다.
     * @param productIds 조회할 Product ID 목록 (Auction 엔티티의 productId 타입과 일치 - 현재 Integer)
     * @param pageable 페이징 정보
     * @return 해당 Product ID들을 가진 Auction 목록 페이지
     */
    Page<Auction> findByProductIdIn(List<Integer> productIds, Pageable pageable);


}
