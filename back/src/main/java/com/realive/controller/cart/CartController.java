package com.realive.controller.cart;

import com.realive.dto.cart.CartItemAddRequestDTO;
import com.realive.dto.cart.CartItemResponseDTO;
import com.realive.dto.cart.CartListResponseDTO;
import com.realive.dto.cart.CartItemUpdateRequestDTO;
import com.realive.service.cart.crud.CartService;
import com.realive.service.cart.view.CartViewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@Log4j2
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartViewService cartViewService;

    private Long getAuthenticatedCustomerId() {
        return 1L; // 임시 고객 ID
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponseDTO> addCartItem(
            @Valid @RequestBody CartItemAddRequestDTO requestDTO
    ) {
        Long customerId = getAuthenticatedCustomerId();
        log.info("API: Add item to cart for customer {}: Product ID = {}, Quantity = {}",
                customerId, requestDTO.getProductId(), requestDTO.getQuantity());
        CartItemResponseDTO response = cartService.addCartItem(customerId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemResponseDTO> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequestDTO requestDTO
    ) {
        Long customerId = getAuthenticatedCustomerId();
        log.info("API: Update quantity for cart item {} for customer {}: New Quantity = {}",
                cartItemId, customerId, requestDTO.getQuantity());
        CartItemResponseDTO response = cartService.updateCartItemQuantity(customerId, cartItemId, requestDTO);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
        Long customerId = getAuthenticatedCustomerId();
        log.info("API: Removing cart item {} for customer {}", cartItemId, customerId);
        cartService.removeCartItem(customerId, cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        Long customerId = getAuthenticatedCustomerId();
        log.info("API: Clearing cart for customer {}", customerId);
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CartListResponseDTO> getCart() {
        Long customerId = getAuthenticatedCustomerId();
        log.info("API: Getting cart for customer {}", customerId);
        CartListResponseDTO response = cartViewService.getCart(customerId);
        return ResponseEntity.ok(response);
    }
}