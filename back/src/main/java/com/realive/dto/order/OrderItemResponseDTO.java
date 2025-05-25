package com.realive.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// OrderResponseDTO 안에 포함될 개별 주문 항목(상품)의 정보 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {
    private long productId;
    private String productName;
    private int quantity;
    private int price; // 개별 상품 항목의 가격
    private String imageUrl;
}