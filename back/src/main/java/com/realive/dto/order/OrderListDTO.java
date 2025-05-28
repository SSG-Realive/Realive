package com.realive.dto.order;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 목록 응답 DTO
 * - 판매자 마이페이지 등에서 사용
 */
@Data
@Builder
public class OrderListDTO {

    // 주문 ID
    private Long orderId;

    // 상품명
    private String productName;

    // 주문 수량
    private int quantity;

    // 총 가격
    private int totalPrice;

    // 주문 일시
    private LocalDateTime orderDate;

    // 주문 상태 (예: 결제완료, 배송중 등)
    private String orderStatus;

    private List<ProductInfoDTO> products;


}