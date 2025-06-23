package com.realive.service.payment;

import com.realive.domain.payment.Payment;
import com.realive.domain.order.Order; // Order를 리턴할 수도 있으므로 필요
import com.realive.dto.payment.TossPaymentApproveRequestDTO;

// 결제 관련 비즈니스 로직을 정의하는 인터페이스
public interface PaymentService {

    /**
     * 토스페이먼츠 결제를 승인하고 Payment 엔티티를 저장합니다.
     *
     * @param request 프론트에서 전달받은 결제 키, 주문 ID, 금액 정보
     * @return 승인 완료 후 저장된 Payment 엔티티
     */
    Payment approveTossPayment(TossPaymentApproveRequestDTO request);

    /**
     * 특정 주문 ID에 해당하는 결제 정보를 조회합니다.
     *
     * @param orderId 조회할 주문의 ID
     * @return 해당 주문의 Payment 엔티티
     * @throws IllegalArgumentException 결제 정보를 찾을 수 없을 때 발생
     */
    Payment getPaymentByOrderId(Long orderId);

    // 필요하다면 다른 결제 관련 비즈니스 메서드 (예: 환불, 결제 상태 변경 등)를 여기에 추가
    // void refundPayment(Long paymentId, int amount);
    // Payment updatePaymentStatus(Long paymentId, String newStatus);
}