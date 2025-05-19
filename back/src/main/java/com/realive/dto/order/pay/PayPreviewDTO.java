package com.realive.dto.order.pay;

import java.util.List;

import com.realive.dto.order.product.StoreOrderDTO;

import lombok.Data;

//주문 및 결제화면
//주문단위
//배송정보, 결제방법 추가됨 
@Data
public class PayPreviewDTO {

    //배송메세지 추가 요망

    private Long orderId; //주문id
    
    private List<StoreOrderDTO> storeOrders; //스토어별 묶음 상품 정보

    private String receiverName; //배송-받는사람이름

    private String phone;//배송-핸드폰번호

    private String address;//배송-주소

    private String paymentMethod;//결제수단 

    private int totalProductAmount; // 모든 스토어 상품 합산 금액
    private int totalDeliveryFee;   // 모든 스토어 배송비 합산
    private int finalAmount;        // 전체 결제 금액 = 상품합 + 배송비합
}
