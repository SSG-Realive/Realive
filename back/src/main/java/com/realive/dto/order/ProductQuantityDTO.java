package com.realive.dto.order;

import jakarta.validation.constraints.Min; // 추가
import jakarta.validation.constraints.NotNull; // 추가
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductQuantityDTO {
    @NotNull(message = "상품 ID는 필수입니다.") // 상품 ID는 null이 아니어야 함
    private Long productId;
    @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.") // 수량은 1개 이상이어야 함
    private int quantity;
}