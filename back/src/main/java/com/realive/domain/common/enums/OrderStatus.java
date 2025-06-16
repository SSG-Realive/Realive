package com.realive.domain.common.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {

    // 초기 주문 및 결제 관련 상태
    ORDER_RECEIVED("주문접수"),
    PAYMENT_COMPLETED("결제완료"),

    // 취소 및 환불 관련 상태
    PAYMENT_CANCELED("결제취소"),
    PURCHASE_CANCELED("구매취소"),
    REFUND_COMPLETED("환불완료"),

    // 배송 연계 상태 (OrderService 로직에서 참조)
    DELIVERY_PREPARING("배송준비중"),
    DELIVERY_IN_PROGRESS("배송중"),
    DELIVERY_COMPLETED("배송완료"),

    // 최종 구매 확정 상태
    PURCHASE_CONFIRMED("구매확정"),

    // === 고객 계정 삭제(탈퇴)와 관련된 주문 상태 추가 ===
    /**
     * 고객 계정 삭제(탈퇴)로 인해 관리자가 주문을 비활성화하거나 특별 관리해야 할 때 사용될 수 있는 상태입니다.
     * 또는 고객이 탈퇴하기 전에 스스로 주문을 취소했을 때의 상태와 구분될 수 있습니다.
     * 실제 운영 정책에 따라 'CANCELLED_BY_ADMIN_USER_DELETION', 'VOIDED_DUE_TO_USER_DELETION' 등
     * 더 명확한 이름과 설명을 사용할 수 있습니다.
     */
    ORDER_CLOSED_BY_USER_DELETION("고객탈퇴로인한주문종결");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }



}