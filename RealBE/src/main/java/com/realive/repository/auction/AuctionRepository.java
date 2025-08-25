package com.realive.repository.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.common.enums.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Auction 데이터 접근 레포지토리.
 * 기본 CRUD 및 동적 조회 기능을 제공합니다.
 */
@Repository
public interface AuctionRepository extends JpaRepository<Auction, Integer>, JpaSpecificationExecutor<Auction>, AuctionRepositoryCustom {

    /**
     * 특정 관리 상품에 등록된 경매 전체 조회
     */
    List<Auction> findByAdminProduct_Id(Integer adminProductId);

    /**
     * 관리 상품+상태로 경매 조회 (주로 진행 중 경매 1건 찾기에 활용)
     */
    Optional<Auction> findByAdminProduct_IdAndStatus(Integer adminProductId, AuctionStatus status);

    /**
     * 여러 관리 상품의 경매 페이지 조회
     */
    Page<Auction> findByAdminProduct_IdIn(List<Integer> adminProductIds, Pageable pageable);

    /**
     * 특정 시작시간 이후 경매 전체 조회
     */
    List<Auction> findByStartTimeAfter(LocalDateTime startTime);

    /**
     * 특정 종료시간 이전 경매 전체 조회
     */
    List<Auction> findByEndTimeBefore(LocalDateTime endTime);

    /**
     * 특정 상태가 아닌 모든 경매 조회
     */
    List<Auction> findByStatusNot(AuctionStatus status);

    /**
     * 관리 상품 ID + 제외할 상태의 경매 조회
     */
    Optional<Auction> findByAdminProduct_IdAndStatusNot(Integer adminProductId, AuctionStatus status);

    /**
     * Lock을 걸어서 경매 상세 조회 (PESSIMISTIC_WRITE)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdWithLock(@Param("id") Integer id);

    /**
     * 특정 상태면서 종료시간이 지난 경매 전체 조회
     */
    List<Auction> findByStatusAndEndTimeBefore(AuctionStatus status, LocalDateTime endTime);

    /**
     * 특정 낙찰자가 낙찰한 경매 목록을 페이징하여 조회
     */
    Page<Auction> findByWinningCustomerIdAndStatus(Long winningCustomerId, AuctionStatus status, Pageable pageable);

    // === 관리자 대시보드용 통계 메서드들 ===
    
    /**
     * 특정 날짜에 생성된 경매 건수 조회
     */
    @Query("SELECT COUNT(a) FROM Auction a WHERE DATE(a.createdAt) = :date")
    Long countAuctionsByDate(@Param("date") LocalDate date);



    /**
     * 특정 기간에 생성된 경매 건수 조회
     */
    @Query("SELECT COUNT(a) FROM Auction a WHERE DATE(a.createdAt) BETWEEN :startDate AND :endDate")
    Long countAuctionsByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 날짜/시간 범위에 생성된 경매 건수 조회 (시간 포함)
     */
    @Query("SELECT COUNT(a) FROM Auction a WHERE a.createdAt BETWEEN :startDateTime AND :endDateTime")
    Long countAuctionsByDateTime(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 경매별 입찰 수를 계산하는 쿼리
     */
    @Query("SELECT a.id, COUNT(b.id) as bidCount FROM Auction a LEFT JOIN Bid b ON a.id = b.auctionId GROUP BY a.id")
    List<Object[]> getAuctionBidCounts();
}