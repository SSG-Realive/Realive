package com.realive.repository.order;

import com.realive.domain.order.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerOrderDeliveryRepository
        extends JpaRepository<OrderDelivery, Long>, SellerOrderDeliveryRepositoryCustom {

    // ✅ 단건 조회 (orderId + sellerId) → 엔티티 반환
    @Query("""
        SELECT d FROM OrderDelivery d
        JOIN d.order o
        WHERE o.id = :orderId
          AND EXISTS (
              SELECT 1 FROM OrderItem oi
              WHERE oi.order = o
              AND oi.product.seller.id = :sellerId
          )
    """)
    Optional<OrderDelivery> findByOrderIdAndSellerId(
            @Param("orderId") Long orderId,
            @Param("sellerId") Long sellerId
    );

    // ✅ 전체 조회 (sellerId 기준) → 엔티티 반환
    @Query("""
        SELECT DISTINCT d FROM OrderDelivery d
        JOIN d.order o
        WHERE EXISTS (
            SELECT 1 FROM OrderItem oi
            WHERE oi.order = o
            AND oi.product.seller.id = :sellerId
        )
    """)
    List<OrderDelivery> findAllBySellerId(@Param("sellerId") Long sellerId);
}
