package com.realive.dto.cart;

import lombok.Data;

@Data
public class CartItemUpdateRequestDTO {

    private Long productId;
    private int quantity;

}
