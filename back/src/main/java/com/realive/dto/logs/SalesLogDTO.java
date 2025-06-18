package com.realive.dto.logs;

import com.realive.domain.logs.SalesLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SalesLogDTO {
    private final Integer id;
    private final Integer orderItemId;
    private final Integer productId;
    private final Integer sellerId;
    private final Integer customerId;
    private final Integer quantity;
    private final Integer unitPrice;
    private final Integer totalPrice;
    private final LocalDate soldAt;

    public static SalesLogDTO fromEntity(SalesLog entity) {
        return SalesLogDTO.builder()
                .id(entity.getId())
                .orderItemId(entity.getOrderItemId())
                .productId(entity.getProductId())
                .sellerId(entity.getSellerId())
                .customerId(entity.getCustomerId())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .totalPrice(entity.getTotalPrice())
                .soldAt(entity.getSoldAt())
                .build();
    }
}

