package com.realive.repository.logs;

import com.realive.domain.logs.CommissionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CommissionLogRepository extends JpaRepository<CommissionLog, Integer> {

    Optional<CommissionLog> findById(Integer id);

    @Query("select cl.id, cl.salesLogId, cl.commissionRate, cl.commissionAmount, " +
            "  cl.recordedAt " +
            " from CommissionLog cl ")
    Page<Object[]> list1(Pageable pageable);

    // 특정 판매 로그 ID에 해당하는 커미션 조회
    Optional<CommissionLog> findBySalesLogId(Integer salesLogId);

    // 특정 날짜의 판매에 대한 총 커미션 금액
    @Query("SELECT SUM(cl.commissionAmount) FROM CommissionLog cl JOIN SalesLog sl ON cl.salesLogId = sl.id WHERE sl.soldAt = :date")
    Integer sumCommissionAmountByDate(LocalDate date);

    // 특정 판매자의 특정 기간 커미션 금액
    @Query("SELECT SUM(cl.commissionAmount) FROM CommissionLog cl JOIN SalesLog sl ON cl.salesLogId = sl.id WHERE sl.sellerId = :sellerId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumCommissionAmountBySellerAndDateRange(Integer sellerId, LocalDate startDate, LocalDate endDate);

    /**
     * 여러 SalesLog ID에 해당하는 CommissionLog를 한 번에 조회
     */
    List<CommissionLog> findBySalesLogIdIn(List<Integer> salesLogIds);

    /**
     * 특정 기간의 총 수수료 금액 조회 (판매자별 필터링 없음)
     */
    @Query("SELECT COALESCE(SUM(cl.commissionAmount), 0) FROM CommissionLog cl " +
            "JOIN SalesLog sl ON cl.salesLogId = sl.id " +
            "WHERE sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumCommissionAmountByDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
