package com.realive.dto.admin.adminsettlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 판매 상세 응답
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDetailResponseDTO {
    private Integer salesLogId;
    private Integer orderItemId;
    private Integer productId;
    private Integer quantity;
    private Integer unitPrice;
    private Integer totalPrice;
    private LocalDate soldAt;
}