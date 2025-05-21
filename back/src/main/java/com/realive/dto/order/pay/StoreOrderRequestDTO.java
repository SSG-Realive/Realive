package com.realive.dto.order.pay;

import java.util.List;

import lombok.Data;

//스토어별 결제 요청 정보
@Data
public class StoreOrderRequestDTO {

    private Long sellerId;
    private int deliveryFee;

    private List<OrderProductRequestDTO> products; // 스토어 내 상품들

    private int storeTotalPrice;   // 스토어 상품 가격 합
    private int storeFinalPrice;   // storeTotalPrice + deliveryFee
}