package com.realive.repository.logs;

import com.realive.domain.logs.PenaltyLog;
import com.realive.domain.logs.SalesLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SalesLogRepository extends JpaRepository<SalesLog, Integer> {

    Optional<SalesLog> findById(Integer id);

    @Query("select sl.id, sl.orderItemId, sl.productId, sl.sellerId, sl.customerId, sl.quantity," +
            " sl.unitPrice, sl.totalPrice, sl.soldAt " +
            " from SalesLog sl ")
    Page<Object[]> list1(Pageable pageable);
}
