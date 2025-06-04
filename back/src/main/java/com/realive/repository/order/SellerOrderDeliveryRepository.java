package com.realive.repository.order;

import com.realive.domain.order.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerOrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> , SellerOrderDeliveryRepositoryCustom {

   
}