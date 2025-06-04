package com.realive.dto.cart;

import com.realive.domain.customer.CartItem;
import com.realive.dto.product.ProductResponseDTO;
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

    private Long cartItemId;
    private Long productId;
    private String productName;
    private int quantity;
    private int productPrice;
    private String productImage;
    private int totalPrice; // quantity * productPrice
    private LocalDateTime cartCreatedAt;

    // ProductResponseDTO를 받아서 DTO를 생성하는 정적 팩토리 메서드
    public static CartItemResponseDTO from(CartItem cartItem, ProductResponseDTO productDetailDto) {
        return CartItemResponseDTO.builder()
                .cartItemId(cartItem.getId())
                .productId(productDetailDto != null ? productDetailDto.getId() : null)
                .productName(productDetailDto != null ? productDetailDto.getName() : null)
                .quantity(cartItem.getQuantity())
                .productPrice(productDetailDto != null ? productDetailDto.getPrice() : 0)
                .productImage(productDetailDto != null ? productDetailDto.getImageThumbnailUrl() : null)
                // builder로 totalPrice를 바로 계산해서 반환
                .totalPrice(productDetailDto != null ? productDetailDto.getPrice() * cartItem.getQuantity() : 0)
                .cartCreatedAt(cartItem.getCreatedAt())
                .build();
    }
}