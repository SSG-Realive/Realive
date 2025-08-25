package com.realive.repository.order;


import com.realive.dto.order.OrderStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.realive.dto.order.SellerOrderListDTO;
import com.realive.dto.order.SellerOrderSearchCondition;

public interface SellerOrderDeliveryRepositoryCustom {

    OrderStatisticsDTO getOrderStatisticsBySeller(Long sellerId);
    Page<SellerOrderListDTO> getOrderListBySeller(
        Long sellerId,
        SellerOrderSearchCondition condition,
        Pageable pageable
    );


}
