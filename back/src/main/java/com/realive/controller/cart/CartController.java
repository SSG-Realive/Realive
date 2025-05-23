package com.realive.controller.cart;

import com.realive.dto.cart.CartItemAddRequestDTO;
import com.realive.dto.cart.CartItemResponseDTO;
import com.realive.dto.cart.CartItemUpdateRequestDTO;
import com.realive.dto.cart.CartListResponseDTO;
import com.realive.service.cart.crud.CartService;
import com.realive.service.cart.view.CartViewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Log4j2
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartViewService cartViewService;

    // TODO: 현재 Security가 완성되지 않아 테스트를 위한 임시 customerId를 사용.
    //  추후 Security 완성시 Authenticated CustomerId를 가져오는것으로 코드 변경 필수
    private Long getAuthenticatedCustomerId() {
        return 1L; // 임시 customerId
    }

    //장바구니 추가
    @PostMapping
    public ResponseEntity<CartItemResponseDTO> addCartItem(@Valid @RequestBody CartItemAddRequestDTO requestDTO) {
        log.info("Request to add item to cart: {}", requestDTO);
        Long customerId = getAuthenticatedCustomerId();
        CartItemResponseDTO response = cartService.addCartItem(customerId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //장바구니 리스트 조회
    @GetMapping
    public ResponseEntity<CartListResponseDTO> getCart() {
        log.info("Request to view cart list.");
        Long customerId = getAuthenticatedCustomerId();
        CartListResponseDTO response = cartViewService.getCart(customerId);
        return ResponseEntity.ok(response);
    }

    //장바구니 수량 변경
    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponseDTO> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequestDTO requestDTO) {
        log.info("Request to update cart item {}: {}", cartItemId, requestDTO);
        Long customerId = getAuthenticatedCustomerId();
        CartItemResponseDTO response = cartService.updateCartItemQuantity(customerId, cartItemId, requestDTO);

        if (response == null) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(response);
    }

    //장바구니 물품 삭제
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
        log.info("Request to remove cart item: {}", cartItemId);
        Long customerId = getAuthenticatedCustomerId();
        cartService.removeCartItem(customerId, cartItemId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    //장바구니 물품 모두 삭제
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        log.info("Request to clear cart.");
        Long customerId = getAuthenticatedCustomerId();
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}