package com.realive.repository.order;

import com.realive.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // 주문 조회
    Optional<Order> findByCustomer_IdAndId(Long customerId, Long id);

    // 모든 주문 조회
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer ORDER BY o.OrderedAt DESC",
            countQuery = "SELECT count(o) FROM Order o")
    Page<Order> findAllOrders(Pageable pageable);
}