package com.realive.dto.order;

import lombok.Data;

@Data
public class OrderAddRequestDTO {

    private long orderId;
    private long customerId;
    private long productId;
    private int quantity;
    private String paymentType;

}
