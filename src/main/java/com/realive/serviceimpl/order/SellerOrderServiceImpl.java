package com.realive.serviceimpl.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.realive.dto.order.SellerOrderListDTO;
import com.realive.dto.order.SellerOrderSearchCondition;
import com.realive.repository.order.SellerOrderDeliveryRepository;
import com.realive.service.order.SellerOrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {

    private final SellerOrderDeliveryRepository sellerOrderDeliveryRepository;

    @Override
    public Page<SellerOrderListDTO> getOrderListBySeller(Long sellerId, SellerOrderSearchCondition condition,
            Pageable pageable) {
        
        return sellerOrderDeliveryRepository.getOrderListBySeller(sellerId, condition, pageable);
    }
    
}
