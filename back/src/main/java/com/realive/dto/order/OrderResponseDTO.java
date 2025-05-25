package com.realive.dto.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.order.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private long orderId;
    private long customerId;
    private String deliveryAddress;
    private int deliveryFee;
    private int totalPrice;
    private String paymentType;
    private String deliveryStatus;
    private String orderStatus;
    private LocalDateTime orderCreatedAt;

    private List<OrderItemResponseDTO> orderItems;

    // 주문 전체의 요약 정보를 위한 from 메서드 (OrderListResponseDTO에서 사용)
    public static OrderResponseDTO fromOrder(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .deliveryAddress(order.getDeliveryAddress())
                .totalPrice(order.getTotalPrice())
                .paymentType(order.getPaymentType())
                .orderStatus(order.getStatus().getDescription())
                .orderCreatedAt(order.getOrderedAt())
                .build();
    }

    // 단일 주문 상세 조회를 위한 from 메서드 (OrderServiceImpl의 getOrder에서 사용)
    public static OrderResponseDTO from(
            Order order,
            List<OrderItemResponseDTO> orderItemDTOs,
            int totalDeliveryFee,
            String deliveryStatus
    ) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .deliveryAddress(order.getDeliveryAddress())
                .totalPrice(order.getTotalPrice())
                .paymentType(order.getPaymentType())
                .orderStatus(order.getStatus().getDescription())
                .orderCreatedAt(order.getOrderedAt())
                .deliveryFee(totalDeliveryFee)
                .deliveryStatus(deliveryStatus)
                .orderItems(orderItemDTOs)
                .build();
    }
}