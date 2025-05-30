package com.realive.repository.order;

import com.realive.domain.order.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerOrderDeliveryRepository
        extends JpaRepository<OrderDelivery, Long>, SellerOrderDeliveryRepositoryCustom {

    // 🔧 [수정된 부분] 주문 ID 단건 조회 (판매자 검증 포함)
    @Query("""
        SELECT d FROM OrderDelivery d
        JOIN d.order o
        JOIN o.orderItems oi
        JOIN oi.product p
        WHERE o.id = :orderId AND p.seller.id = :sellerId
    """)
    Optional<OrderDelivery> findByOrderIdAndSellerId(@Param("orderId") Long orderId, @Param("sellerId") Long sellerId);

    // 🔁 전체 배송 목록 (판매자 기준)
    @Query("""
        SELECT DISTINCT d FROM OrderDelivery d
        JOIN d.order o
        JOIN o.orderItems oi
        JOIN oi.product p
        WHERE p.seller.id = :sellerId
    """)
    List<OrderDelivery> findAllBySellerId(@Param("sellerId") Long sellerId);
}