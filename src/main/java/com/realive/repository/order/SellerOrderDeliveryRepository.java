package com.realive.repository.order;

import com.realive.domain.order.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerOrderDeliveryRepository extends JpaRepository<OrderDelivery, Long>, SellerOrderDeliveryRepositoryCustom {
    
    @Query("SELECT od FROM OrderDelivery od " +
           "JOIN od.order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE o.id = :orderId AND p.seller.id = :sellerId")
    Optional<OrderDelivery> findByOrderIdAndSellerId(@Param("orderId") Long orderId, @Param("sellerId") Long sellerId);

    @Query("SELECT od FROM OrderDelivery od " +
           "JOIN od.order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId")
    List<OrderDelivery> findAllBySellerId(@Param("sellerId") Long sellerId);
}