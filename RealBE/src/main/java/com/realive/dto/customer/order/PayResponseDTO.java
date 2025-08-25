package com.realive.dto.customer.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayResponseDTO {
    private String orderId;
    private String paymentKey;
    private String status;
    private Long totalAmount;
    private String orderName;
    private String approvedAt;
    private String message;
} 