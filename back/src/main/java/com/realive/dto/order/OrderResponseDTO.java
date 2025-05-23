package com.realive.dto.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.common.enums.PaymentType;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.DeliveryPolicy;
import com.realive.domain.product.Product;
import com.realive.domain.product.ProductImage;
import com.realive.dto.product.ProductResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private long orderId;
    private long customerId;
    private long productId;
    private String productName;
    private int quantity;
    private int price;
    private String imageUrl;
    private String deliveryAddress;
    private int deliveryFee;
    private int totalPrice;
    private String paymentType;
    private String deliveryStatus;
    private String OrderStatus;
    private LocalDateTime orderCreatedAt;

    public static OrderResponseDTO from(
            Order order, OrderItem orderItem, Product product, ProductImage productImage,
            DeliveryPolicy deliveryPolicy, PaymentType paymentType, DeliveryStatus deliveryStatus,
            OrderStatus orderStatus
             ) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .productId(orderItem.getId())
                .productName(product.getName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .imageUrl(productImage.getUrl())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryFee(deliveryPolicy.getCost())
                .totalPrice(orderItem.getPrice() * orderItem.getQuantity() + deliveryPolicy.getCost())
                .paymentType(paymentType.getDescription())
                .deliveryStatus(deliveryStatus.getDescription())
                .OrderStatus(orderStatus.getDescription())
                .orderCreatedAt(order.getOrderedAt())
                .build();
    }
}
