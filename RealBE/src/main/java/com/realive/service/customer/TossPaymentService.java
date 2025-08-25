package com.realive.service.customer;

import com.realive.dto.payment.TossPaymentResponse;

public interface TossPaymentService {
    
    /**
     * 토스페이먼츠 v2 결제 승인
     */
    TossPaymentResponse confirmPayment(String paymentKey, String orderId, Long amount);
    
    /**
     * 결제 정보 조회
     */
    TossPaymentResponse getPayment(String paymentKey);
    
    /**
     * 결제 취소
     */
    TossPaymentResponse cancelPayment(String paymentKey, String cancelReason);
} 