package com.realive.repository.order;

import com.realive.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // @Query 사용을 위해 추가
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // 특정 주문 ID에 속하는 모든 주문 항목 조회
    // OrderItem의 order 필드는 ManyToOne으로 Order를 참조하므로, order.id로 조회
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(Long orderId);

    // 여러 주문 ID에 속하는 모든 주문 항목 조회 (N+1 방지)
    // Product 엔티티를 fetch join하여 OrderItem 조회 시 Product 정보도 함께 가져옴
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.order.id IN :orderIds")
    List<OrderItem> findByOrderIdIn(List<Long> orderIds);
}