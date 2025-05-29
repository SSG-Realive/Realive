package com.realive.serviceimpl.seller;

import com.realive.domain.order.Order;
import com.realive.dto.order.OrderListDTO;
import com.realive.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerOrderServiceImpl implements SellerOrderService {

    private final OrderRepository orderRepository;

    @Override
    public Page<OrderListDTO> getOrdersBySeller(Long sellerId, Pageable pageable) {
        // 판매자 ID 기준 주문 조회
        Page<Order> orders = orderRepository.findOrdersBySellerId(sellerId, pageable);

        // OrderItem 기준으로 DTO 생성
        List<OrderListDTO> dtoList = orders.stream()
                .flatMap(order -> order.getOrderItems().stream()
                        .filter(item -> item.getProduct().getSeller().getId().equals(sellerId))
                        .map(item -> OrderListDTO.builder()
                                .orderId(order.getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .totalPrice(item.getPrice() * item.getQuantity())
                                .orderDate(order.getOrderedAt())
                                .orderStatus(order.getStatus().name())
                                .build())
                )
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, orders.getTotalElements());
    }
}
