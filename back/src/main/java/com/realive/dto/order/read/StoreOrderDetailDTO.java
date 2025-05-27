package com.realive.dto.order.read;

import java.util.List;

import lombok.Data;

//스토어단위 상품 정보 
@Data
public class StoreOrderDetailDTO {

    private Long storeId;
    private String storeName;

    private int deliveryFee;
    private int storeTotalPrice;
    private int storeFinalPrice;

    private String storeOrderStatus;

    private List<OrderProductDetailDTO> products;
}
