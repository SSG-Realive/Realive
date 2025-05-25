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
    Optional<Order> findByCustomerIdAndOrderId(Long customerId, Long orderId);

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer ORDER BY o.orderedAt DESC",
            countQuery = "SELECT count(o) FROM Order o")
    Page<Order> findAllOrders(Pageable pageable);
}