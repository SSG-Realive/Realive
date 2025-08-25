package com.realive.service.order;

import com.realive.dto.order.OrderStatisticsDTO;
import com.realive.dto.order.SellerOrderListDTO;
import com.realive.dto.order.SellerOrderSearchCondition;
import com.realive.dto.seller.SellerOrderDetailResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerOrderService {
    Page<SellerOrderListDTO> getOrderListBySeller(Long sellerId, SellerOrderSearchCondition condition, Pageable pageable);
    SellerOrderDetailResponseDTO getOrderDetail(Long sellerId, Long orderId);
    OrderStatisticsDTO getOrderStatistics(Long sellerId);
}
