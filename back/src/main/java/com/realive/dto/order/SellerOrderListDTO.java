package com.realive.dto.order;

import java.time.LocalDateTime;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.DeliveryType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerOrderListDTO {

    private Long orderId;
    private LocalDateTime orderedAt;
    private String customerName;
    private String productName;
    private int quantity;

    private DeliveryStatus deliveryStatus;
    private String deleiveryStatusText;

    private String trackingNumber;
    private LocalDateTime startDate;
    private LocalDateTime completeDate;

    private DeliveryType deliveryType;

    // Projection 생성자
    public SellerOrderListDTO(
            Long orderId,
            String productName,
            String customerName,
            int quantity,
            DeliveryStatus deliveryStatus,
            String trackingNumber,
            LocalDateTime startDate,
            LocalDateTime completeDate,
            LocalDateTime orderedAt
    ) {
        this.orderId = orderId;
        this.productName = productName;
        this.customerName = customerName;
        this.quantity = quantity;
        this.deliveryStatus = deliveryStatus;
        this.trackingNumber = trackingNumber;
        this.startDate = startDate;
        this.completeDate = completeDate;
        this.orderedAt = orderedAt;
    }
}
