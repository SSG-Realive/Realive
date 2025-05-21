package com.realive.repository.logs;

import com.realive.domain.logs.PayoutLog;
import com.realive.domain.logs.SalesLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PayoutLogRepository extends JpaRepository<PayoutLog, Integer> {

    Optional<PayoutLog> findById(Integer id);

    @Query("select pol.id, pol.sellerId, pol.periodStart, pol.periodEnd, pol.totalSales, pol.totalCommission," +
            " pol.payoutAmount, pol.processedAt " +
            " from PayoutLog pol ")
    Page<Object[]> list1(Pageable pageable);
}
