package com.realive.repository.auction;

import com.realive.domain.auction.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository; // Spring 빈으로 등록 (생략 가능)

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Auction 엔티티에 대한 데이터 접근 계층(Repository) 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공하며,
 * JpaSpecificationExecutor를 상속받아 동적인 조건으로 데이터를 조회하는 기능을 지원합니다.
 */
@Repository // 이 어노테이션은 JpaRepository를 상속하면 보통 생략 가능합니다.
public interface AuctionRepository extends JpaRepository<Auction, Integer>, JpaSpecificationExecutor<Auction> {

    /**
     * 특정 상품 ID(productId)에 해당하는 모든 경매 목록을 조회합니다.
     * 한 상품이 여러 번 경매에 부쳐질 수 있는 경우 사용될 수 있습니다.
     * @param productId 조회할 상품의 ID.
     * @return 해당 상품 ID를 가진 경매(Auction) 엔티티 목록. 결과가 없으면 빈 리스트를 반환합니다.
     */
    List<Auction> findByProductId(Integer productId);

    /**
     * 특정 상품 ID(productId)에 대해 현재 종료되지 않은(isClosed = false) 경매를 조회합니다.
     * 일반적으로 시스템 정책상 하나의 상품에 대해 동시에 진행 중인 경매는 하나만 허용될 때 유용합니다.
     * @param productId 조회할 상품의 ID.
     * @return Optional 객체로 감싸진, 조건에 맞는 Auction 엔티티. 조건에 맞는 경매가 없으면 Optional.empty()를 반환합니다.
     */
    Optional<Auction> findByProductIdAndIsClosedFalse(Integer productId);

    /**
     * 여러 상품 ID(productId) 목록에 해당하는 경매들을 페이징 처리하여 조회합니다.
     * 이 메소드는 주로 특정 판매자가 등록한 모든 상품들의 경매 목록을 가져올 때,
     * AuctionServiceImpl의 getAuctionsBySeller 메소드 내부에서 사용됩니다.
     * @param productIds 조회할 상품 ID(Integer 타입)의 목록.
     * @param pageable 페이징 및 정렬 정보를 담고 있는 Pageable 객체.
     * @return 해당 상품 ID들을 가진 Auction 엔티티의 페이지(Page) 객체.
     */
    Page<Auction> findByProductIdIn(List<Integer> productIds, Pageable pageable);

    // --- 아래의 메소드들은 JpaSpecificationExecutor를 사용하여 AuctionServiceImpl에서 동적으로 조건을 조합하여
    // --- 조회하는 경우, 중복되거나 필요성이 낮아질 수 있습니다.
    // --- 프로젝트의 구체적인 조회 패턴과 복잡성에 따라 유지하거나 제거 또는 수정할 수 있습니다. ---

    /**
     * (참고용 또는 특정 상황용) 현재 진행 중인 경매 목록을 페이징하여 조회합니다.
     * '종료되지 않았고 (isClosed = false)' 그리고 '종료 시간이 현재 시간 이후 (endTime > currentTime)'인 경매를 찾습니다.
     * AuctionServiceImpl의 getActiveAuctions 메소드에서 Specification을 사용하면 이 메소드는 대체될 수 있습니다.
     * @param currentTime 현재 시간 (LocalDateTime).
     * @param pageable 페이징 및 정렬 정보를 담고 있는 Pageable 객체.
     * @return 조건에 맞는 Auction 엔티티의 페이지(Page) 객체.
     */
    // Page<Auction> findByIsClosedFalseAndEndTimeAfter(LocalDateTime currentTime, Pageable pageable); // 현재 AuctionServiceImpl에서는 Specification으로 대체됨

    /**
     * (참고용 또는 특정 상황용) 특정 시작 시간(startTime) 이후에 시작된 모든 경매 목록을 조회합니다.
     * @param startTime 조회 기준 시작 시간.
     * @return 조건에 맞는 Auction 엔티티 목록.
     */
    List<Auction> findByStartTimeAfter(LocalDateTime startTime);

    /**
     * (참고용 또는 특정 상황용) 특정 종료 시간(endTime) 이전에 종료된 모든 경매 목록을 조회합니다.
     * @param endTime 조회 기준 종료 시간.
     * @return 조건에 맞는 Auction 엔티티 목록.
     */
    List<Auction> findByEndTimeBefore(LocalDateTime endTime);

    /**
     * (참고용 또는 특정 상황용) 아직 종료되지 않은(isClosed = false) 모든 경매 목록을 조회합니다.
     * @return 조건에 맞는 Auction 엔티티 목록.
     */
    List<Auction> findByIsClosedFalse();

    /**
     * (참고용 또는 특정 상황용) 이미 종료된(isClosed = true) 모든 경매 목록을 조회합니다.
     * @return 조건에 맞는 Auction 엔티티 목록.
     */
    List<Auction> findByIsClosedTrue();

    /**
     * (참고용 또는 특정 상황용) 특정 현재 가격(currentPrice) 이상인 모든 경매 목록을 조회합니다.
     * @param price 조회 기준 현재 가격.
     * @return 조건에 맞는 Auction 엔티티 목록.
     */
    List<Auction> findByCurrentPriceGreaterThanEqual(Integer price);
}
