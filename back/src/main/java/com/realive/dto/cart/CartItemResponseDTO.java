package com.realive.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {

    private Long cartId; // 장바구니 번호
    private Long productId; //물품 번호, product쪽 dto가 합쳐지고 필요시 수정
    private String productName; // 물품 이름, product쪽 dto가 합쳐지고 필요시 수정
    private int quantity; // 물품 수량, product쪽 dto가 합쳐지고 필요시 수정
    private int productPrice; // 물품 가격, product쪽 dto가 합쳐지고 필요시 수정
    private String productImage; // 물품 이미지, product쪽 dto가 합쳐지고 필요시 수정
    private int totalPrice; // 총액
    private LocalDateTime cartCreatedAt; // 장바구니 생성 시간

}
