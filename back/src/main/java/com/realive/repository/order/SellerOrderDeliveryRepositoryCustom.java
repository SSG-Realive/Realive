package com.realive.repository.order;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.realive.dto.order.SellerOrderListDTO;
import com.realive.dto.order.SellerOrderSearchCondition;

public interface SellerOrderDeliveryRepositoryCustom {
    
    Page<SellerOrderListDTO> getOrderListBySeller(
        Long sellerId,
        SellerOrderSearchCondition condition,
        Pageable pageable
    );
}
