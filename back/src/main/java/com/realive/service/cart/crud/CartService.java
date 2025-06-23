package com.realive.service.cart.crud;

import com.realive.dto.cart.CartItemAddRequestDTO;
import com.realive.dto.cart.CartItemResponseDTO;
import com.realive.dto.cart.CartItemUpdateRequestDTO;
import com.realive.dto.order.PayRequestDTO; // PayRequestDTO를 OrderService와 CartService 모두에서 사용

public interface CartService {

    CartItemResponseDTO addCartItem(Long customerId, CartItemAddRequestDTO requestDTO);

    CartItemResponseDTO updateCartItemQuantity(Long customerId, Long cartItemId, CartItemUpdateRequestDTO requestDTO);

    void removeCartItem(Long customerId, Long cartItemId);

    void clearCart(Long customerId);

    // 장바구니 다수 상품 결제 진행 및 구매내역 생성 (CartService의 새로운 책임)
    Long processCartPayment(PayRequestDTO payRequestDTO);
}