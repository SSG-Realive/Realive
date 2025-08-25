package com.realive.controller.customer;

import com.realive.dto.payment.TossPaymentConfirmRequest;
import com.realive.dto.payment.TossPaymentResponse;
import com.realive.service.customer.TossPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/payments/toss")
@RequiredArgsConstructor
@Slf4j
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;

    /**
     * 토스페이먼츠 v2 결제 승인
     */
    @PostMapping("/confirm")
    public ResponseEntity<TossPaymentResponse> confirmPayment(
            @RequestBody TossPaymentConfirmRequest request) {
        
        log.info("토스페이먼츠 v2 결제 승인 요청: paymentKey={}, orderId={}, amount={}", 
                request.getPaymentKey(), request.getOrderId(), request.getAmount());
        
        try {
            TossPaymentResponse response = tossPaymentService.confirmPayment(
                    request.getPaymentKey(), 
                    request.getOrderId(), 
                    request.getAmount()
            );
            
            log.info("토스페이먼츠 v2 결제 승인 성공: paymentKey={}", request.getPaymentKey());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("토스페이먼츠 v2 결제 승인 실패: paymentKey={}, error={}", 
                    request.getPaymentKey(), e.getMessage());
            throw e;
        }
    }

    /**
     * 결제 정보 조회
     */
    @GetMapping("/{paymentKey}")
    public ResponseEntity<TossPaymentResponse> getPayment(@PathVariable String paymentKey) {
        log.info("토스페이먼츠 결제 정보 조회: paymentKey={}", paymentKey);
        
        try {
            TossPaymentResponse response = tossPaymentService.getPayment(paymentKey);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("토스페이먼츠 결제 정보 조회 실패: paymentKey={}, error={}", 
                    paymentKey, e.getMessage());
            throw e;
        }
    }

    /**
     * 결제 취소
     */
    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<TossPaymentResponse> cancelPayment(
            @PathVariable String paymentKey,
            @RequestParam String cancelReason) {
        
        log.info("토스페이먼츠 결제 취소 요청: paymentKey={}, reason={}", paymentKey, cancelReason);
        
        try {
            TossPaymentResponse response = tossPaymentService.cancelPayment(paymentKey, cancelReason);
            log.info("토스페이먼츠 결제 취소 성공: paymentKey={}", paymentKey);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("토스페이먼츠 결제 취소 실패: paymentKey={}, error={}", 
                    paymentKey, e.getMessage());
            throw e;
        }
    }
} 