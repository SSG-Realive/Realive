package com.realive.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponseDTO {

    private List<OrderResponseDTO> orders; // 각 OrderResponseDTO는 이제 여러 OrderItemResponseDTO를 포함할 수 있음
    private int totalItems;
    private int totalOrderPrice;

    public static OrderListResponseDTO from(List<OrderResponseDTO> orders) {
        // totalItems와 totalOrderPrice 계산 로직 수정 필요:
        // OrderResponseDTO가 이제 OrderItemResponseDTO 리스트를 가지고 있으므로,
        // 각 주문의 총 항목 수와 총 가격을 합산해야 합니다.

        int totalItems = orders.stream()
                .flatMap(order -> order.getOrderItems().stream()) // 각 주문의 OrderItems 스트림을 평탄화
                .mapToInt(OrderItemResponseDTO::getQuantity) // 각 OrderItem의 수량 합산
                .sum();

        int totalOrderPrice = orders.stream()
                .mapToInt(OrderResponseDTO::getTotalPrice) // 각 주문의 총 가격 합산 (OrderResponseDTO의 totalPrice)
                .sum();

        return OrderListResponseDTO.builder()
                .orders(orders)
                .totalItems(totalItems)
                .totalOrderPrice(totalOrderPrice)
                .build();
    }
}