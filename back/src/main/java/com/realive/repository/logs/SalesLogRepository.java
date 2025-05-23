package com.realive.repository.logs;

import com.realive.domain.logs.SalesLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesLogRepository extends JpaRepository<SalesLog, Integer>, JpaSpecificationExecutor<SalesLog> {

    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.soldAt = :date")
    Integer sumTotalPriceByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(sl) FROM SalesLog sl WHERE sl.soldAt = :date")
    Integer countBySoldAt(@Param("date") LocalDate date);

    @Query("SELECT SUM(sl.quantity) FROM SalesLog sl WHERE sl.soldAt = :date")
    Integer sumQuantityByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumTotalPriceBySoldAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT sl.orderItemId) FROM SalesLog sl WHERE sl.soldAt BETWEEN :startDate AND :endDate")
    Long countDistinctOrdersBySoldAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Page<SalesLog> findByCustomerId(Integer customerId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT sl.orderItemId) FROM SalesLog sl WHERE sl.customerId = :customerId")
    Integer countDistinctOrdersByCustomerId(@Param("customerId") Integer customerId);

    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.customerId = :customerId")
    Integer sumTotalPriceByCustomerId(@Param("customerId") Integer customerId);

    Page<SalesLog> findBySellerId(Integer sellerId, Pageable pageable);

    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.sellerId = :sellerId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumTotalPriceBySellerIdAndSoldAtBetween(@Param("sellerId") Integer sellerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT sl.orderItemId) FROM SalesLog sl WHERE sl.sellerId = :sellerId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Long countDistinctOrdersBySellerIdAndSoldAtBetween(@Param("sellerId") Integer sellerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Page<SalesLog> findByProductId(Integer productId, Pageable pageable);

    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.productId = :productId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumTotalPriceByProductIdAndSoldAtBetween(@Param("productId") Integer productId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(sl.quantity) FROM SalesLog sl WHERE sl.productId = :productId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumQuantityByProductIdAndSoldAtBetween(@Param("productId") Integer productId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<SalesLog> findBySoldAtBetween(LocalDate startDate, LocalDate endDate);

}
