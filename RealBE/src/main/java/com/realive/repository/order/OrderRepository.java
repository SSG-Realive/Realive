package com.realive.repository.order;

import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByCustomerIdAndId(Long customerId, Long id);

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    @Query(value = """
            SELECT o FROM Order o
            JOIN FETCH o.customer
            ORDER BY o.orderedAt DESC
            """,
            countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllOrders(Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT oi.order)
        FROM OrderItem oi
        JOIN oi.product p
        WHERE p.seller.id = :sellerId
        AND oi.order.status IN :statuses
    """)
    long countInProgressOrders(@Param("sellerId") Long sellerId, @Param("statuses") Collection<OrderStatus> statuses);

    List<Order> findAllByCustomerId(Long customerId);

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = 'COMPLETED'
        AND DATE(o.orderedAt) BETWEEN :startDate AND :endDate
        ORDER BY o.orderedAt DESC
    """)
    List<Order> findCompletedOrdersBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = 'COMPLETED'
        AND DATE(o.orderedAt) = :date
        ORDER BY o.orderedAt DESC
    """)
    List<Order> findCompletedOrdersByDate(@Param("date") LocalDate date);

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = 'DELIVERED'
        AND DATE(o.orderedAt) BETWEEN :startDate AND :endDate
        ORDER BY o.orderedAt DESC
    """)
    List<Order> findDeliveredOrdersBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.orderItems oi
        JOIN FETCH o.customer
        JOIN FETCH oi.product p
        JOIN FETCH p.seller
        WHERE p.seller.id = :sellerId   
        AND o.id = :orderId
    """)
    Optional<Order> findBySellerIdAndOrderId(@Param("sellerId") Long sellerId, @Param("orderId") Long orderId);

    Optional<Order> findFirstByCustomerIdOrderByOrderedAtDesc(Long customerId);


}
