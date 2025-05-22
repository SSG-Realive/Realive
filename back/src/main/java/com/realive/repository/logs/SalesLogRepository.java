package com.realive.repository.logs;

import com.realive.domain.logs.SalesLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesLogRepository extends JpaRepository<SalesLog, Integer> {

    Optional<SalesLog> findById(Integer id);

    @Query("select sl.id, sl.orderItemId, sl.productId, sl.sellerId, sl.customerId, sl.quantity," +
            " sl.unitPrice, sl.totalPrice, sl.soldAt " +
            " from SalesLog sl ")
    Page<Object[]> list1(Pageable pageable);

    // 특정 날짜의 총 판매 금액
    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.soldAt = :date")
    Integer sumTotalPriceByDate(LocalDate date);

    // 특정 날짜의 총 판매 건수
    @Query("SELECT COUNT(sl) FROM SalesLog sl WHERE sl.soldAt = :date")
    Integer countByDate(LocalDate date);

    // 특정 날짜의 총 판매 수량
    @Query("SELECT SUM(sl.quantity) FROM SalesLog sl WHERE sl.soldAt = :date")
    Integer sumQuantityByDate(LocalDate date);

    // 특정 기간의 판매 내역 조회
    List<SalesLog> findBySoldAtBetween(LocalDate startDate, LocalDate endDate);

    // 특정 월의 판매 내역 조회
    @Query("SELECT sl FROM SalesLog sl WHERE YEAR(sl.soldAt) = :year AND MONTH(sl.soldAt) = :month")
    List<SalesLog> findByYearAndMonth(int year, int month);

    // 특정 판매자의 특정 날짜 판매 내역
    List<SalesLog> findBySellerIdAndSoldAt(Integer sellerId, LocalDate date);

    // 특정 상품의 특정 날짜 판매 내역
    List<SalesLog> findByProductIdAndSoldAt(Integer productId, LocalDate date);
}
