package com.realive.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 주문 내 상품 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private int unitPrice;
    private int totalPrice;
}
