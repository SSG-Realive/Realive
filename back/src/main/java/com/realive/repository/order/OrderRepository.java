package com.realive.repository.order;

import com.realive.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // 고객 ID와 주문 ID로 주문을 조회하는 메서드 (기존 이름 유지)
    Optional<Order> findByCustomerIdAndOrderId(Long customerId, Long orderId);

    // 모든 주문 목록 조회를 위한 @Query (OrderServiceImpl의 getOrderList()에서 사용)
    // Order 엔티티와 그와 관련된 Customer 엔티티를 fetch join하여 N+1 문제 완화
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer ORDER BY o.orderedAt DESC",
            countQuery = "SELECT count(o) FROM Order o")
    Page<Order> findAllOrders(Pageable pageable);
}