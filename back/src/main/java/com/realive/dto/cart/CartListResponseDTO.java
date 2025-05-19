package com.realive.dto.cart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartListResponseDTO {

    private List<CartItemResponseDTO> items; // 장바구니 항목 리스트
    private int totalItems; // 총 상품 수
    private int totalCartPrice; // 장바구니 총액

}