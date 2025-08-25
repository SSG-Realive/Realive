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
    private String method;
    private String status;
    private Long totalAmount;
    private String type;
    private String currency;
    private String customerKey;
    private String lastTransactionKey;
    private LocalDateTime approvedAt;
    private LocalDateTime requestedAt;
}
