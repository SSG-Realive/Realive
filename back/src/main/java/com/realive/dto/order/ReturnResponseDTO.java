package com.realive.dto.order;

import com.realive.domain.common.enums.ReturnReason;
import com.realive.domain.common.enums.ReturnStatus;
import com.realive.domain.order.ReturnRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReturnResponseDTO {
    private Long returnRequestId;
    private Long orderId;
    private Long productId;
    private String productName;
    private int quantity;
    private String returnReasonDescription;
    private String detailedReason;
    private List<String> imageUrls;
    private String statusDescription;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private int refundAmount;
    private int returnShippingFee;
    private String adminMemo;

    public static ReturnResponseDTO from(ReturnRequest returnRequest, String productName) {
        return ReturnResponseDTO.builder()
                .returnRequestId(returnRequest.getId())
                .orderId(returnRequest.getOrder().getId())
                .productId(returnRequest.getProduct().getId())
                .productName(productName)
                .quantity(returnRequest.getQuantity())
                .returnReasonDescription(returnRequest.getReturnReason().getDescription())
                .detailedReason(returnRequest.getDetailedReason())
                .imageUrls(returnRequest.getImageUrls())
                .statusDescription(returnRequest.getStatus().getDescription())
                .requestedAt(returnRequest.getCreatedAt())
                .processedAt(returnRequest.getProcessedAt())
                .refundAmount(returnRequest.getRefundAmount())
                .returnShippingFee(returnRequest.getReturnShippingFee())
                .adminMemo(returnRequest.getAdminMemo())
                .build();
    }
}