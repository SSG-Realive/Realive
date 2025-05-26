package com.realive.domain.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReturnStatus {
    // 고객 신청 및 초기 상태
    PENDING("반품 요청 접수"),             // 고객이 반품 요청을 한 상태
    REVIEWING("반품 접수 대기 중"),             // 관리자가 요청을 검토 중. 단순 변심/하자 구분 및 배송비 책정을 위한 단계.

    // 승인 후 처리 단계 (모든 요청은 승인됨)
    APPROVED("반품 승인 및 회수 예정"),   // 반품이 승인되어 상품 회수 준비
    COLLECTING("상품 회수 중"),           // 상품 회수가 진행 중
    COLLECTED("상품 회수 완료"),           // 상품이 물류창고/판매자에게 회수 완료
    REFUND_PROCESSING("환불 처리 중"),    // 환불이 진행 중
    COMPLETED("반품 완료"),             // 반품 처리 및 환불 완료

    // 고객에 의한 요청 철회
    CANCELED_BY_CUSTOMER("고객에 의한 요청 철회");

    private final String description;
}