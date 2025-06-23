package com.realive.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TossPaymentApproveResponseDTO {
    private String version;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String method; // 결제 수단 (카드, 가상계좌 등)
    private String status; // 결제 상태 (DONE, READY, CANCELED 등)
    private Long totalAmount; // Long으로 받는 것이 안전
    private String type; // 결제 타입 (NORMAL, BILLING, BRANDPAY 등)
    private String currency;
    private String customerKey;
    private String lastTransactionKey;
    private LocalDateTime approvedAt;
    private LocalDateTime requestedAt;
    // 추가적으로 필요한 필드 (예: card, virtualAccount 등 상세 정보)
}
