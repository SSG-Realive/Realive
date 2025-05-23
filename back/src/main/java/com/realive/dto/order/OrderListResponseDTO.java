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

    private List<OrderResponseDTO> orders;
    private int totalItems;
    private int totalOrderPrice;

    public static OrderListResponseDTO from(List<OrderResponseDTO> orders) {

        int totalItems = orders.stream().mapToInt(OrderResponseDTO::getQuantity).sum();
        int totalOrderPrice = orders.stream().mapToInt(OrderResponseDTO::getTotalPrice).sum();

        return OrderListResponseDTO.builder()
                .orders(orders)
                .totalItems(totalItems)
                .totalOrderPrice(totalOrderPrice)
                .build();
    }
}
