package com.realive.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemUpdateRequestDTO {

    private int quantity;

}
