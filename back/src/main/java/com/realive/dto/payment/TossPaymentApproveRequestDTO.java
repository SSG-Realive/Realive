package com.realive.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentApproveRequestDTO {
    private String paymentKey;
    private String orderId;
    private Long amount; // String이 아닌 Long으로 받는 것이 더 적합
}
