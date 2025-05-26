package com.realive.repository.order;

import com.realive.domain.order.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {

    // ✅ 주문 ID를 기준으로 배송 정보를 조회
    Optional<OrderDelivery> findByOrderId(Long orderId);

    // ✅ 판매자 기준 배송 목록 (이미 있는 메서드)
    @Query("SELECT d FROM OrderDelivery d " +
            "JOIN d.order o " +
            "JOIN o.product p " +
            "WHERE p.seller.id = :sellerId")
    List<OrderDelivery> findAllBySellerId(@Param("sellerId") Long sellerId);
}