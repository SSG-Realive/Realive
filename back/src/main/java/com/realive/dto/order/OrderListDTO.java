//package com.realive.dto.order;
//
//import lombok.Builder;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//
///**
// * 판매자가 본인의 주문 목록을 확인할 때 사용하는 DTO
// * - 엔티티 노출 방지
// * - 필요한 필드만 프론트에 전달
// */
//@Data
//@Builder
//public class OrderListDTO {
//
//    private Long orderId;                // 주문 고유 ID
//    private String productName;          // 주문한 상품의 이름
//    private int quantity;                // 주문 수량
//    private int totalPrice;              // 총 결제 금액 (단가 × 수량)
//    private LocalDateTime orderDate;     // 주문 생성일 (주문 시간)
//    private String orderStatus;          // 주문 상태 (결제대기, 결제완료 등)
//    private String deliveryStatus;       // 배송 상태 (배송중, 배송완료 등)
//    private LocalDateTime deliveryStartDate;    // 배송 시작일
//    private LocalDateTime deliveryCompleteDate; // 배송 완료일
//}