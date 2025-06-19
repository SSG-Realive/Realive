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
    private int totalItems; // 총 상품 수 (장바구니 내 항목의 총 개수)
    private int totalCartPrice; // 장바구니 총액 (모든 상품 가격 합계)

    // DTO 변환 시 총액 및 총 상품 수 계산
    public static CartListResponseDTO from(List<CartItemResponseDTO> items) {
        int totalItems = items.stream().mapToInt(CartItemResponseDTO::getQuantity).sum();
        //totalPrice 참조로 가져옴
        int totalCartPrice = items.stream().mapToInt(CartItemResponseDTO::getTotalPrice).sum();

        return CartListResponseDTO.builder()
                .items(items)
                .totalItems(totalItems)
                .totalCartPrice(totalCartPrice)
                .build();
    }
}