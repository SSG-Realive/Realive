package com.realive.dto.auction;

import jakarta.validation.constraints.NotNull;
import lombok.*;

// 상품 상태 업데이트 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStatusUpdateRequestDTO {
    @NotNull(message = "상품 ID는 필수입니다.")
    private Integer productId;

    @NotNull(message = "상품 상태는 필수입니다.")
    private String productStatus;  // AVAILABLE, PENDING, SOLD, DEFECTIVE 등

    private String statusNote;     // 상태 변경 이유
}