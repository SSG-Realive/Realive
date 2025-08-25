package com.realive.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectPaymentInfoDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer price;
    private Integer totalPrice;
    private Integer deliveryFee;
    private String imageUrl;
    private String receiverName;
    private String phone;
    private String deliveryAddress;
} 