package com.realive.repository.payment;

import com.realive.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findByOrderId(Long orderId); // 주문 ID로도 찾을 수 있도록 추가 (Order 엔티티에 tossOrderId 필드가 있을 경우)
}
