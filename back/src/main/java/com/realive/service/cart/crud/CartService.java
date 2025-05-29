package com.realive.service.cart.crud;

import com.realive.dto.cart.CartItemAddRequestDTO;
import com.realive.dto.cart.CartItemResponseDTO;
import com.realive.dto.cart.CartItemUpdateRequestDTO;

public interface CartService {

    CartItemResponseDTO addCartItem(Long customerId, CartItemAddRequestDTO requestDTO);

    CartItemResponseDTO updateCartItemQuantity(Long customerId, Long cartItemId, CartItemUpdateRequestDTO requestDTO);

    void removeCartItem(Long customerId, Long cartItemId);

    void clearCart(Long customerId);
}