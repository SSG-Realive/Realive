package com.realive.dto.order.read;

import java.time.LocalDateTime;
import java.util.List;

import com.realive.dto.order.product.OrderProductDTO;

import lombok.Data;

//구매내역 상세 조회
@Data
public class OrderDetailDTO {

    private Long orderId;
    private LocalDateTime orderDate;

    private String receiverName;
    private String phone;
    private String address;

    private String paymentMethod;
    private int totalProductAmount;
    private int totalDeliveryFee;
    private int finalAmount;

    private List<StoreOrderDetailDTO> storeOrders;
}
