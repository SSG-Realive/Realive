package com.realive.service.order;

import com.realive.dto.order.SellerOrderListDTO;
import com.realive.dto.order.SellerOrderSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerOrderService {
    Page<SellerOrderListDTO> getOrderListBySeller(Long sellerId, SellerOrderSearchCondition condition, Pageable pageable);
    
}
