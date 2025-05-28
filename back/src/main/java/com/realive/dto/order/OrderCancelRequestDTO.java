package com.realive.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCancelRequestDTO {
    private Long orderId;
    private Long customerId;
    private String reason; // 취소 사유
}