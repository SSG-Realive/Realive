package com.realive.repository.order;

import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 고객 ID와 주문 ID로 단건 조회
    Optional<Order> findByCustomer_IdAndId(Long customerId, Long id);

    // 관리자 전체 주문 목록 조회 (Customer 정보 포함)
    @Query(value = """
            SELECT o FROM Order o
            JOIN FETCH o.customer
            ORDER BY o.orderedAt DESC
            """,
            countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllOrders(Pageable pageable);

    // 판매자 기준 진행 중인 주문 수 조회
@Query("""
    SELECT COUNT(DISTINCT oi.order)
    FROM OrderItem oi
    JOIN oi.product p
    WHERE p.seller.id = :sellerId
    AND oi.order.status IN :statuses
""")
long countInProgressOrders(@Param("sellerId") Long sellerId, @Param("statuses") Collection<OrderStatus> statuses);
}