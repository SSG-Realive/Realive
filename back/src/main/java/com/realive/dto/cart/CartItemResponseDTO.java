package com.realive.dto.cart;

import com.realive.domain.customer.CartItem;
// import com.realive.domain.product.Product; // 더 이상 Product 엔티티를 직접 받지 않음
import com.realive.dto.product.ProductResponseDTO; // ProductResponseDto 임포트
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
    private String productImage; // 썸네일 이미지 URL
    private int totalPrice; // quantity * productPrice
    private LocalDateTime cartCreatedAt;

    // ProductResponseDto를 받아서 DTO를 생성하는 정적 팩토리 메서드
    public static CartItemResponseDTO from(CartItem cartItem, ProductResponseDTO productDetailDto) {
        return CartItemResponseDTO.builder()
                .cartItemId(cartItem.getId())
                .productId(productDetailDto != null ? productDetailDto.getId() : null)
                .productName(productDetailDto != null ? productDetailDto.getName() : null)
                .quantity(cartItem.getQuantity())
                .productPrice(productDetailDto != null ? productDetailDto.getPrice() : 0)
                .productImage(productDetailDto != null ? productDetailDto.getImageThumbnailUrl() : null) // ProductResponseDto에서 썸네일 URL 가져옴
                .totalPrice(productDetailDto != null ? productDetailDto.getPrice() * cartItem.getQuantity() : 0)
                .cartCreatedAt(cartItem.getCreatedAt())
                .build();
    }
}