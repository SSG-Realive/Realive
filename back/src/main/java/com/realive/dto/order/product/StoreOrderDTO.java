package com.realive.dto.order.product;

import java.util.List;

import lombok.Data;

//스토어(판매자) 단위의 주문 상품 정보(결제전전)
@Data
public class StoreOrderDTO {

    private Long sellerId;

    private List<OrderProductDTO> products;

    private int deliveryFee;    //스토어별 배송비
    private int storeTotalPrice; //스토어 상품 총액
    private int storeFinalPrice; //상품 총액 + 배송비
    
}
