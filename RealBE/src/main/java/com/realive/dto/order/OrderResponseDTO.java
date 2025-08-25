package com.realive.dto.order;

import com.realive.domain.customer.Customer;
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
    private int totalPrice;
    private String orderStatus;
    private LocalDateTime orderCreatedAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponseDTO> orderItems;
    private int deliveryFee;

    // Customer 엔티티에서 가져오는 정보
    private String receiverName;
    private String phone;

    // Order 엔티티에 직접 존재하지 않지만, DTO에서 필요한 필드들
    // 이 값들은 서비스 계층에서 추가적인 조회/계산 후 DTO에 주입될 것입니다.
    private String paymentType;  // 결제 방식
    private String deliveryStatus; // 배송 상태 (DeliveryStatus enum의 description 활용)

    public static OrderResponseDTO fromOrder(Order order) {

        Customer customer = order.getCustomer();

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .customerId(customer.getId())
                .receiverName(customer.getName()) // Customer 엔티티의 name 필드와 매핑
                .phone(customer.getPhone())       // Customer 엔티티의 phone 필드와 매핑
                .deliveryAddress(order.getDeliveryAddress())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getStatus().getDescription()) // OrderStatus enum의 description 사용
                .orderCreatedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }


    public static OrderResponseDTO from(
            Order order,
            List<OrderItemResponseDTO> orderItemDTOs,
            int totalDeliveryFee,
            String paymentType,
            String deliveryStatus
    ) {

        Customer customer = order.getCustomer();

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .customerId(customer.getId())
                .receiverName(customer.getName()) // Customer 엔티티의 name 필드와 매핑
                .phone(customer.getPhone())       // Customer 엔티티의 phone 필드와 매핑
                .deliveryAddress(order.getDeliveryAddress())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getStatus().getDescription())
                .orderCreatedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(orderItemDTOs)
                .deliveryFee(totalDeliveryFee)
                .paymentType(paymentType)
                .deliveryStatus(deliveryStatus)
                .build();
    }
}