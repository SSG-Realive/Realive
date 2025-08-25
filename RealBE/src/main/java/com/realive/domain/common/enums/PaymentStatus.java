package com.realive.domain.common.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    READY("결제 준비"),          // 결제 요청은 되었으나 아직 승인 전
    IN_PROGRESS("결제 진행중"),   // 가상계좌 발급 등 결제가 진행되고 있는 상태
    WAITING_FOR_DEPOSIT("입금 대기중"), // 가상계좌 결제 등 입금을 기다리는 상태
    COMPLETED("결제 완료"),     // 결제가 최종적으로 성공한 상태 (DONE)
    CANCELED("결제 취소"),       // 결제가 취소된 상태
    PARTIAL_CANCELED("부분 취소"), // 일부 금액만 취소된 상태
    REFUNDED("환불 완료"),      // 전체 금액이 환불된 상태
    FAILED("결제 실패");         // 결제 실패

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
}