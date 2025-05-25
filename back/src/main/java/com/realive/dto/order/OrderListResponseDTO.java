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


        int totalItems = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToInt(OrderItemResponseDTO::getQuantity)
                .sum();

        int totalOrderPrice = orders.stream()
                .mapToInt(OrderResponseDTO::getTotalPrice)
                .sum();

        return OrderListResponseDTO.builder()
                .orders(orders)
                .totalItems(totalItems)
                .totalOrderPrice(totalOrderPrice)
                .build();
    }
}