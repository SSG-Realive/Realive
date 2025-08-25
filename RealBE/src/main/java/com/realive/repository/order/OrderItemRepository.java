package com.realive.repository.order;

import com.realive.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

 // 단일 주문 id로 주문 상품 전체 조회 + product + seller 정보 포함
 @Query("""
        SELECT oi
        FROM OrderItem oi
        JOIN FETCH oi.product p
        JOIN FETCH p.seller
        WHERE oi.order.id = :orderId
    """)
 List<OrderItem> findByOrderId(Long orderId);

 // 다수 주문 id로 주문 상품 전체 조회 + product + seller 정보 포함
 @Query("""
        SELECT oi
        FROM OrderItem oi
        JOIN FETCH oi.product p
        JOIN FETCH p.seller
        WHERE oi.order.id IN :orderIds
    """)
 List<OrderItem> findByOrderIdIn(List<Long> orderIds);

 /**
  * 주문에 포함된 판매자 ID 목록 조회 (중복 제거)
  */
 @Query("""
                SELECT DISTINCT p.seller.id
                FROM OrderItem oi
                JOIN oi.product p
                WHERE oi.order.id = :orderId
            """)
 List<Long> findSellerIdsByOrderId(@Param("orderId") Long orderId);

 /**
  * 배송완료된 주문 중, 특정 판매자의 정산 기간 내 총 매출 합산
  */
 @Query("""
                SELECT SUM(oi.price)
                FROM OrderItem oi
                JOIN oi.product p
                JOIN oi.order o
                JOIN OrderDelivery od ON od.order.id = o.id
                WHERE p.seller.id = :sellerId
                    AND od.status = 'DELIVERY_COMPLETED'
                    AND od.updatedAt BETWEEN :startDate AND :endDate
            """)
 Integer sumDeliveredSalesBySellerAndPeriod(
         @Param("sellerId") Long sellerId,
         @Param("startDate") LocalDateTime startDate,
         @Param("endDate") LocalDateTime endDate);

 /**
  * 배송완료된 주문 중, 특정 판매자의 정산 기간 내 판매건 상세 조회
  */
 @Query("""
    SELECT oi
    FROM OrderItem oi
    JOIN FETCH oi.product p
    JOIN FETCH p.seller
    JOIN FETCH oi.order o         
    JOIN OrderDelivery od ON od.order.id = o.id
    WHERE p.seller.id = :sellerId
        AND od.status = 'DELIVERY_COMPLETED'
        AND od.updatedAt BETWEEN :startDate AND :endDate
    ORDER BY od.updatedAt DESC
""")
 List<OrderItem> findDeliveredBySellerAndPeriod(
         @Param("sellerId") Long sellerId,
         @Param("startDate") LocalDateTime startDate,
         @Param("endDate") LocalDateTime endDate);



}