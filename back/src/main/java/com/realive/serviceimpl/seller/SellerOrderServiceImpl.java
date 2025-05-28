package com.realive.serviceimpl.seller;

import com.realive.domain.order.Order;
import com.realive.domain.seller.Seller;
import com.realive.dto.order.OrderListDTO;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.seller.SellerRepository;
import com.realive.service.seller.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerOrderServiceImpl implements SellerOrderService {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;

    @Override
    public Page<OrderListDTO> getOrdersBySeller(String email, Pageable pageable) {
        // 이메일로 판매자 조회
        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        // 주문 조회 + DTO 변환
        Page<Order> orders = orderRepository.findByProductSellerId(seller.getId(), pageable);

        return orders.map(order -> OrderListDTO.builder()
                .orderId(order.getId())
                .productName(order.getProduct().getName())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .orderDate(order.getOrderedAt())
                .orderStatus(order.getStatus().name())
                .build());
    }
}
