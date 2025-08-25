package com.realive.domain.common.enums;

public enum PaymentFailureReason {
    INSUFFICIENT_BALANCE, // 잔액 부족
    INVALID_CARD,         // 유효하지 않은 카드
    TIMEOUT,              // 결제 시간 초과
    PG_ERROR,             // PG사 오류
    USER_CANCELLED,       // 사용자 취소
    UNKNOWN               // 알 수 없는 오류
}