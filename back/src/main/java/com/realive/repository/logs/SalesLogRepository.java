package com.realive.repository.logs;

import com.realive.domain.logs.SalesLog;
import com.realive.dto.logs.salessum.CategorySalesSummaryDTO;
import com.realive.dto.logs.salessum.DailySalesSummaryDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.dto.logs.stats.SellerSalesDetailDTO;
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

    /**
     * 특정 날짜의 총 판매 금액 합계 조회
     * @param date 조회할 날짜
     * @return 해당 날짜의 총 판매 금액
     */
    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.soldAt = :date")
    Integer sumTotalPriceByDate(@Param("date") LocalDate date);

    /**
     * 특정 날짜의 총 판매 로그 건수 조회
     * (DailySalesSummaryDTO의 totalSalesCount 등에 사용)
     * @param date 조회할 날짜
     * @return 해당 날짜의 판매 로그 건수
     */
    @Query("SELECT COUNT(sl) FROM SalesLog sl WHERE sl.soldAt = :date")
    Integer countBySoldAt(@Param("date") LocalDate date);

    /**
     * 특정 날짜의 총 판매 상품 수량 합계 조회
     * @param date 조회할 날짜
     * @return 해당 날짜의 총 판매 상품 수량
     */
    @Query("SELECT SUM(sl.quantity) FROM SalesLog sl WHERE sl.soldAt = :date")
    Integer sumQuantityByDate(@Param("date") LocalDate date);

    /**
     * 특정 기간 동안의 총 판매 금액 합계 조회
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 해당 기간의 총 판매 금액
     */
    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumTotalPriceBySoldAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 기간 동안의 고유 주문 건수 조회
     * (MonthlySalesSummaryDTO의 totalSalesCount 등에 사용)
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 해당 기간의 고유 주문 건수
     */
    @Query("SELECT COUNT(DISTINCT sl.orderItemId) FROM SalesLog sl WHERE sl.soldAt BETWEEN :startDate AND :endDate")
    Long countDistinctOrdersBySoldAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 고객 ID에 해당하는 판매 로그 목록을 페이징하여 조회
     * @param customerId 고객 ID
     * @param pageable 페이징 정보
     * @return 해당 고객의 판매 로그 페이지
     */
    Page<SalesLog> findByCustomerId(Integer customerId, Pageable pageable);

    /**
     * 특정 고객의 고유 주문 건수 조회
     * @param customerId 고객 ID
     * @return 해당 고객의 고유 주문 건수
     */
    @Query("SELECT COUNT(DISTINCT sl.orderItemId) FROM SalesLog sl WHERE sl.customerId = :customerId")
    Integer countDistinctOrdersByCustomerId(@Param("customerId") Integer customerId);

    /**
     * 특정 고객의 총 판매 금액 합계 조회
     * @param customerId 고객 ID
     * @return 해당 고객의 총 판매 금액
     */
    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.customerId = :customerId")
    Integer sumTotalPriceByCustomerId(@Param("customerId") Integer customerId);

    /**
     * 특정 판매자 ID에 해당하는 판매 로그 목록을 페이징하여 조회
     * @param sellerId 판매자 ID
     * @param pageable 페이징 정보
     * @return 해당 판매자의 판매 로그 페이지
     */
    Page<SalesLog> findBySellerId(Integer sellerId, Pageable pageable);

    /**
     * 특정 판매자의 특정 기간 동안의 총 판매 금액 합계 조회
     * @param sellerId 판매자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 해당 판매자 및 기간의 총 판매 금액
     */
    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.sellerId = :sellerId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumTotalPriceBySellerIdAndSoldAtBetween(@Param("sellerId") Integer sellerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 판매자의 특정 기간 동안의 고유 주문 건수 조회
     * @param sellerId 판매자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 해당 판매자 및 기간의 고유 주문 건수
     */
    @Query("SELECT COUNT(DISTINCT sl.orderItemId) FROM SalesLog sl WHERE sl.sellerId = :sellerId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Long countDistinctOrdersBySellerIdAndSoldAtBetween(@Param("sellerId") Integer sellerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 상품 ID에 해당하는 판매 로그 목록을 페이징하여 조회
     * @param productId 상품 ID
     * @param pageable 페이징 정보
     * @return 해당 상품의 판매 로그 페이지
     */
    Page<SalesLog> findByProductId(Integer productId, Pageable pageable);

    /**
     * 특정 상품의 특정 기간 동안의 총 판매 금액 합계 조회
     * @param productId 상품 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 해당 상품 및 기간의 총 판매 금액
     */
    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.productId = :productId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumTotalPriceByProductIdAndSoldAtBetween(@Param("productId") Integer productId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 상품의 특정 기간 동안의 총 판매 상품 수량 합계 조회
     * @param productId 상품 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 해당 상품 및 기간의 총 판매 상품 수량
     */
    @Query("SELECT SUM(sl.quantity) FROM SalesLog sl WHERE sl.productId = :productId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumQuantityByProductIdAndSoldAtBetween(@Param("productId") Integer productId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 기간 동안의 모든 판매 로그 목록 조회
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 해당 기간의 판매 로그 리스트
     */
    List<SalesLog> findBySoldAtBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 특정 판매자의 특정 날짜 총 판매 건수 (SalesLog 기준)
     * DailySalesSummaryDTO의 totalSalesCount에 해당
     */
    @Query("SELECT COUNT(sl) FROM SalesLog sl WHERE sl.sellerId = :sellerId AND sl.soldAt = :date")
    Integer countBySellerIdAndSoldAt(@Param("sellerId") Integer sellerId, @Param("date") LocalDate date);

    /**
     * 특정 판매자의 특정 날짜 총 판매 금액
     * DailySalesSummaryDTO의 totalSalesAmount에 해당
     */
    @Query("SELECT SUM(sl.totalPrice) FROM SalesLog sl WHERE sl.sellerId = :sellerId AND sl.soldAt = :date")
    Integer sumTotalPriceBySellerIdAndSoldAt(@Param("sellerId") Integer sellerId, @Param("date") LocalDate date);

    /**
     * 특정 판매자의 특정 날짜 총 판매 수량
     * DailySalesSummaryDTO의 totalQuantity에 해당
     */
    @Query("SELECT SUM(sl.quantity) FROM SalesLog sl WHERE sl.sellerId = :sellerId AND sl.soldAt = :date")
    Integer sumQuantityBySellerIdAndSoldAt(@Param("sellerId") Integer sellerId, @Param("date") LocalDate date);

    /**
     * 특정 기간 동안 판매된 모든 상품의 총 수량 합계
     * MonthlySalesSummaryDTO의 totalQuantity에 해당
     */
    @Query("SELECT SUM(sl.quantity) FROM SalesLog sl WHERE sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumQuantityBySoldAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 판매자의 특정 기간 동안 판매된 모든 상품의 총 수량 합계
     * MonthlySalesSummaryDTO의 totalQuantity에 해당 (판매자별)
     */
    @Query("SELECT SUM(sl.quantity) FROM SalesLog sl WHERE sl.sellerId = :sellerId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Integer sumQuantityBySellerIdAndSoldAtBetween(@Param("sellerId") Integer sellerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 상품의 특정 날짜 총 판매 건수 (SalesLog 기준)
     * DailySalesSummaryDTO의 totalSalesCount에 해당 (상품별)
     */
    @Query("SELECT COUNT(sl) FROM SalesLog sl WHERE sl.productId = :productId AND sl.soldAt = :date")
    Integer countByProductIdAndSoldAt(@Param("productId") Integer productId, @Param("date") LocalDate date);

    /**
     * 특정 상품의 특정 기간 총 판매 건수 (SalesLog 기준)
     * MonthlySalesSummaryDTO의 totalSalesCount에 해당 (상품별)
     */
    @Query("SELECT COUNT(sl) FROM SalesLog sl WHERE sl.productId = :productId AND sl.soldAt BETWEEN :startDate AND :endDate")
    Integer countByProductIdAndSoldAtBetween(@Param("productId") Integer productId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 날짜의 모든 판매 로그 목록 조회
     * (getDailySalesLogDetails 메소드에서 사용)
     */
    List<SalesLog> findBySoldAt(LocalDate soldAt);

    /**
     * 특정 기간 동안의 카테고리별 판매 현황(건수, 금액)을 집계합니다.
     * SalesLog의 productId를 사용하여 Product 엔티티와 조인하고,
     * Product 엔티티의 category 필드를 사용하여 Category 엔티티와 조인합니다.
     *
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 카테고리별 판매 요약 DTO 리스트
     */
    @Query("SELECT new com.realive.dto.logs.salessum.CategorySalesSummaryDTO(" +
            "c.id, " +                      // categoryId (Long)
            "c.name, " +                    // categoryName (String)
            "COUNT(sl.id), " +              // totalSalesCount (Long)
            "SUM(sl.totalPrice), " +        // totalSalesAmount (Long) - DTO 필드도 Long으로 가정
            "CAST(0 AS java.lang.Integer)) " + // totalProfitAmount (Integer, 임시로 0)
            "FROM SalesLog sl " +
            "JOIN Product p ON sl.productId = p.id " +
            "JOIN p.category c " +
            "WHERE sl.soldAt BETWEEN :startDate AND :endDate " +
            "GROUP BY c.id, c.name " +
            "ORDER BY SUM(sl.totalPrice) DESC")
    List<CategorySalesSummaryDTO> findCategorySalesSummaryBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 기존 메소드들은 그대로 두고 아래 메소드만 추가
    /**
     * 특정 기간 동안의 일별 판매 요약을 한 번의 쿼리로 조회
     */
    @Query("SELECT new com.realive.dto.logs.salessum.DailySalesSummaryDTO(" +
            "sl.soldAt, " +
            "CAST(COUNT(sl) AS java.lang.Integer), " +
            "CAST(SUM(sl.totalPrice) AS java.lang.Integer), " +
            "CAST(SUM(sl.quantity) AS java.lang.Integer)) " +
            "FROM SalesLog sl " +
            "WHERE sl.soldAt BETWEEN :startDate AND :endDate " +
            "GROUP BY sl.soldAt " +
            "ORDER BY sl.soldAt")
    List<DailySalesSummaryDTO> getDailySummariesForPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    /**
     * 특정 기간 동안의 판매자별 판매 상세 정보 조회
     */
    @Query("SELECT new com.realive.dto.logs.stats.SellerSalesDetailDTO(" +
            "sl.sellerId, " +
            "s.name, " +
            "COUNT(sl), " +
            "SUM(sl.totalPrice)) " +
            "FROM SalesLog sl " +
            "JOIN Seller s ON sl.sellerId = s.id " +
            "WHERE sl.soldAt BETWEEN :startDate AND :endDate " +
            "GROUP BY sl.sellerId, s.name " +
            "ORDER BY SUM(sl.totalPrice) DESC")
    List<SellerSalesDetailDTO> getSellerSalesDetailsForPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    /**
     * 특정 기간 동안의 월별 판매 요약을 조회 (Object[] 반환)
     */
    @Query("SELECT " +
            "EXTRACT(YEAR FROM sl.soldAt), " +          // ← 이 부분
            "EXTRACT(MONTH FROM sl.soldAt), " +         // ← 이 부분
            "COUNT(DISTINCT sl.orderItemId), " +
            "SUM(sl.totalPrice), " +
            "SUM(sl.quantity) " +
            "FROM SalesLog sl " +
            "WHERE sl.soldAt BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(YEAR FROM sl.soldAt), EXTRACT(MONTH FROM sl.soldAt) " +  // ← 이 부분
            "ORDER BY EXTRACT(YEAR FROM sl.soldAt), EXTRACT(MONTH FROM sl.soldAt)")    // ← 이 부분
    List<Object[]> getMonthlySummariesForPeriodRaw(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT sl.customerId) FROM SalesLog sl WHERE sl.sellerId = :sellerId")
    Long countDistinctCustomersBySellerId(@Param("sellerId") Long sellerId);

    /**
     * 특정 판매자의 일별 매출 추이 조회
     */
    @Query("SELECT sl.soldAt, COUNT(DISTINCT sl.orderItemId), SUM(sl.totalPrice) " +
            "FROM SalesLog sl " +
            "WHERE sl.sellerId = :sellerId AND sl.soldAt BETWEEN :startDate AND :endDate " +
            "GROUP BY sl.soldAt " +
            "ORDER BY sl.soldAt")
    List<Object[]> getDailySalesBySellerId(@Param("sellerId") Long sellerId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

     /**
     * 특정 판매자의 월별 매출 추이 조회
     */
     @Query(
             value = "SELECT CONCAT(EXTRACT(YEAR FROM sl.sold_at), '-', LPAD(CAST(EXTRACT(MONTH FROM sl.sold_at) AS TEXT), 2, '0')) AS yearMonth, " +
                     "COUNT(DISTINCT sl.order_item_id), SUM(sl.total_price) " +
                     "FROM sales_logs sl " +
                     "WHERE sl.seller_id = :sellerId AND sl.sold_at BETWEEN :startDate AND :endDate " +
                     "GROUP BY EXTRACT(YEAR FROM sl.sold_at), EXTRACT(MONTH FROM sl.sold_at) " +  // ← 이 부분 수정
                     "ORDER BY EXTRACT(YEAR FROM sl.sold_at), EXTRACT(MONTH FROM sl.sold_at)",     // ← 이 부분 수정
             nativeQuery = true
     )
    List<Object[]> getMonthlySalesBySellerId(@Param("sellerId") Long sellerId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
