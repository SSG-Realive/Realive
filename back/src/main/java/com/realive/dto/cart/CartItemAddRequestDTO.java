package com.realive.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemAddRequestDTO {

    @NotNull(message = "상품 번호는 필수입니다.")
    private Long productId;
    @Min(value = 1, message = "수량은 1 이상이여야 합니다.")
    private int quantity;

}
