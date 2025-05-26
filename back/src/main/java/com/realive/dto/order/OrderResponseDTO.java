package com.realive.dto.order;

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

    private Long orderId;
    private Long customerId;
    private String deliveryAddress;
    private String receiverName;
    private String phone;
    private int deliveryFee;
    private int totalPrice;
    private String paymentType;
    private String deliveryStatus;
    private String orderStatus;
    private LocalDateTime orderCreatedAt;
    private LocalDateTime updatedAt;

    private List<OrderItemResponseDTO> orderItems;

    private boolean isSellerReviewWritten; // 판매자 리뷰 작성 여부

    public static OrderResponseDTO fromOrder(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .deliveryAddress(order.getDeliveryAddress())
                .receiverName(order.getReceiverName())
                .phone(order.getPhone())
                .totalPrice(order.getTotalPrice())
                .paymentType(order.getPaymentType())
                .orderStatus(order.getStatus().getDescription())
                .deliveryStatus(order.getDeliveryStatus().getDescription())
                .orderCreatedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .isSellerReviewWritten(order.isSellerReviewWritten())
                .build();
    }

    public static OrderResponseDTO from(
            Order order,
            List<OrderItemResponseDTO> orderItemDTOs,
            int totalDeliveryFee
    ) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .deliveryAddress(order.getDeliveryAddress())
                .receiverName(order.getReceiverName())
                .phone(order.getPhone())
                .totalPrice(order.getTotalPrice())
                .paymentType(order.getPaymentType())
                .orderStatus(order.getStatus().getDescription())
                .deliveryStatus(order.getDeliveryStatus().getDescription())
                .orderCreatedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .deliveryFee(totalDeliveryFee)
                .orderItems(orderItemDTOs)
                .isSellerReviewWritten(order.isSellerReviewWritten())
                .build();
    }
}