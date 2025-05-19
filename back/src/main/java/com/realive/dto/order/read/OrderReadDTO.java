package com.realive.dto.order.read;

import java.time.LocalDateTime;
import java.util.List;

import com.realive.dto.order.product.StoreOrderDTO;

import lombok.Data;

//구매내역 조회(리스트)
@Data
public class OrderReadDTO {
    
    private Long orderId;//주문 id

    private LocalDateTime OrderedAt; //주문날짜

    private List<StoreOrderDTO> storeOrders; //스토어별 묶음 상품 정보

    //DB??
    private String orderStatus;// 주문 상태 (예: 결제완료, 배송중, 배송완료

    private List<StoreOrderDetailDTO> storeOrdersSummary; // 스토어별 요약
}
