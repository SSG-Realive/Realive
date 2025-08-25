package com.realive.dto.payment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentResponse {
    
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private Long totalAmount;
    private String method;
    private String approvedAt;
    private String requestedAt;
    private String message;
} 