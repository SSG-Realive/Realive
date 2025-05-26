package com.realive.repository.logs;

import com.realive.domain.logs.PayoutLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PayoutLogRepository extends JpaRepository<PayoutLog, Integer> {

    Optional<PayoutLog> findById(Integer id);

    @Query("select pol.id, pol.sellerId, pol.periodStart, pol.periodEnd, pol.totalSales, pol.totalCommission," +
            " pol.payoutAmount, pol.processedAt " +
            " from PayoutLog pol ")
    Page<Object[]> list1(Pageable pageable);

    // 특정 날짜에 처리된 총 정산 금액
    @Query("SELECT SUM(pl.payoutAmount) FROM PayoutLog pl WHERE DATE(pl.processedAt) = :date")
    Integer sumPayoutAmountByProcessedDate(LocalDate date);

    // 특정 정산 기간에 포함된 모든 정산 내역
    @Query("SELECT pl FROM PayoutLog pl WHERE :date BETWEEN pl.periodStart AND pl.periodEnd")
    List<PayoutLog> findByDateInPeriod(LocalDate date);

    // 특정 판매자의 정산 내역
    List<PayoutLog> findBySellerId(Integer sellerId);

    // 특정 기간 동안 처리된(processedAt 기준) 정산 내역 조회
    List<PayoutLog> findByProcessedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
