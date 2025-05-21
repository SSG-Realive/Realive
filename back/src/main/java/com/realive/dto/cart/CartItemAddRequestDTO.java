package com.realive.dto.cart;

import lombok.Data;

@Data
public class CartItemAddRequestDTO {

    private Long productId; //물품 번호, product쪽 dto가 합쳐지고 필요시 수정
    private int quantity; // 물품 수량, product쪽 dto가 합쳐지고 필요시 수정

}
