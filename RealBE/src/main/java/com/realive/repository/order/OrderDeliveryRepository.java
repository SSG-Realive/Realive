package com.realive.repository.order;

import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;


@Repository
public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {
    Optional<OrderDelivery> findByOrder(Order order);



    @Query("SELECT od FROM OrderDelivery od JOIN FETCH od.order WHERE od.order IN :orders")
    List<OrderDelivery> findByOrderIn(List<Order> orders);
}