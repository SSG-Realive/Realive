package com.realive.dto.order;

import com.realive.domain.customer.Customer; // Customer 엔티티 import (매핑 시 사용)
import com.realive.domain.order.Order;
import com.realive.domain.common.enums.DeliveryStatus; // DeliveryStatus enum import
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
    private int deliveryFee; // 총 배송비 (DeliveryPolicy의 cost 활용)

    // Customer 엔티티에서 가져오는 정보
    private String receiverName; // 수령인 이름 (Customer.name 매핑)
    private String phone;        // 수령인 전화번호 (Customer.phone 매핑)

    // Order 엔티티에 직접 존재하지 않지만, DTO에서 필요한 필드들
    // 이 값들은 서비스 계층에서 추가적인 조회/계산 후 DTO에 주입될 것입니다.
    private String paymentType;  // 결제 방식
    private String deliveryStatus; // 배송 상태 (DeliveryStatus enum의 description 활용)


    /**
     * Order 엔티티에서 직접 매핑 가능한 필드만 사용하여 OrderResponseDTO를 생성합니다.
     * Order 엔티티에 없는 정보(예: 배송비, 결제 방식 등)는 이 메서드에서 설정하지 않습니다.
     *
     * @param order 매핑할 Order 엔티티 객체
     * @return OrderResponseDTO 객체
     */
    public static OrderResponseDTO fromOrder(Order order) {
        // Customer 객체가 Order 내부에 ManyToOne으로 연결되어 있으므로 접근 가능
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
                // orderItems, deliveryFee, paymentType, deliveryStatus 등은 이 메서드에서 설정하지 않음
                .build();
    }

    /**
     * Order 엔티티와 함께 외부에서 계산/조회된 정보들을 받아 OrderResponseDTO를 완성합니다.
     * 이 메서드에서 Order 엔티티에 없는 필드들을 설정합니다.
     *
     * @param order 매핑할 Order 엔티티 객체
     * @param orderItemDTOs 해당 주문에 포함된 OrderItemResponseDTO 리스트
     * @param totalDeliveryFee 계산된 총 배송비 (DeliveryPolicy에서 파생)
     * @param paymentType 결제 방식 (문자열)
     * @param deliveryStatus 배송 상태 (DeliveryStatus enum의 description 문자열)
     * @return OrderResponseDTO 객체
     */
    public static OrderResponseDTO from(
            Order order,
            List<OrderItemResponseDTO> orderItemDTOs,
            int totalDeliveryFee,
            String paymentType,
            String deliveryStatus
    ) {
        // Customer 객체가 Order 내부에 ManyToOne으로 연결되어 있으므로 접근 가능
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
                .orderItems(orderItemDTOs) // 외부에서 주입된 주문 상품 목록
                .deliveryFee(totalDeliveryFee) // 외부에서 계산된 총 배송비
                .paymentType(paymentType)      // 외부에서 주입된 결제 방식
                .deliveryStatus(deliveryStatus) // 외부에서 주입된 배송 상태
                .build();
    }
}