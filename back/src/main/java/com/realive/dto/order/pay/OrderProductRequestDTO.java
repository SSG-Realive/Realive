package com.realive.dto.order.pay;

import lombok.Data;

//개별상품 결제요청정보
@Data
public class OrderProductRequestDTO {

    private Long productId;
    private int price;
    private int quantity;
    private int totalPrice; // price * quantity
}
