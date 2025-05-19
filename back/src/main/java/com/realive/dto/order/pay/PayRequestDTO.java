package com.realive.dto.order.pay;

import java.util.List;

//결제 요청
public class PayRequestDTO {

    private Long memberId; // 결제자(구매자) ID

    private String receiverName;
    private String phone;
    private String address;

    private String paymentMethod; //결제방법

    private List<StoreOrderRequestDTO> storeOrders; // 스토어별 주문 정보

    private int totalProductAmount; // 전체 상품 총액 (모든 스토어의 상품 가격 합)
    private int totalDeliveryFee;   // 전체 배송비 (모든 스토어의 배송비 합)
    private int finalAmount;        // 결제 총액 = 상품 + 배송비
    
}
