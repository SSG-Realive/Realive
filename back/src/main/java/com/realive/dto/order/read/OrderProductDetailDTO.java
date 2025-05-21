package com.realive.dto.order.read;

import lombok.Data;

//구매 개별 상품 정보
@Data
public class OrderProductDetailDTO {
    private Long productId;
    private String productName;
    private String thumbnailUrl;

    private int price;
    private int quantity;
    private int totalPrice;
}
