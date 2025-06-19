package com.realive.controller.cart;

import com.realive.dto.cart.CartItemAddRequestDTO;
import com.realive.dto.cart.CartItemResponseDTO;
import com.realive.dto.cart.CartItemUpdateRequestDTO;
import com.realive.dto.cart.CartListResponseDTO;
import com.realive.dto.customer.member.MemberLoginDTO;
import com.realive.dto.order.PayRequestDTO; // PayRequestDTO 임포트
import com.realive.service.cart.crud.CartService;
import com.realive.service.cart.view.CartViewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/cart")
@Log4j2
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartViewService cartViewService;

    // 장바구니 추가
    @PostMapping
    public ResponseEntity<CartItemResponseDTO> addCartItem(
            @AuthenticationPrincipal MemberLoginDTO userDetails,
            @Valid @RequestBody CartItemAddRequestDTO requestDTO) {
        log.info("장바구니 항목 추가 요청: {}", requestDTO);
        CartItemResponseDTO response = cartService.addCartItem(userDetails.getId(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 장바구니 리스트 조회
    @GetMapping
    public ResponseEntity<CartListResponseDTO> getCart(
            @AuthenticationPrincipal MemberLoginDTO userDetails) {
        log.info("장바구니 목록 조회 요청.");
        CartListResponseDTO response = cartViewService.getCart(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    // 장바구니 수량 변경
    @PatchMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponseDTO> updateCartItem(
            @AuthenticationPrincipal MemberLoginDTO userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequestDTO requestDTO) {
        log.info("장바구니 항목 {} 수량 업데이트 요청: {}", cartItemId, requestDTO);
        CartItemResponseDTO response = cartService.updateCartItemQuantity(
                userDetails.getId(), cartItemId, requestDTO);

        if (response == null) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(response);
    }

    // 장바구니 물품 삭제
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            @AuthenticationPrincipal MemberLoginDTO userDetails,
            @PathVariable Long cartItemId) {
        log.info("장바구니 항목 제거 요청: {}", cartItemId);
        cartService.removeCartItem(userDetails.getId(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    // 장바구니 물품 모두 삭제
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal MemberLoginDTO userDetails) {
        log.info("장바구니 전체 비우기 요청.");
        cartService.clearCart(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * **장바구니 다수 상품 결제 처리**
     * POST /api/customer/cart/payment (경로 변경)
     * @param payRequestDTO 결제 및 주문 생성 요청 DTO (orderItems, paymentKey, tossOrderId 필드 필수)
     * @param userDetails 현재 인증된 사용자 정보
     * @return 생성된 주문의 ID (Long)
     */
    @PostMapping("/payment") // 경로를 /api/customer/cart/payment 로 변경했습니다.
    public ResponseEntity<Long> processCartPayment(
            @RequestBody PayRequestDTO payRequestDTO,
            @AuthenticationPrincipal MemberLoginDTO userDetails) {
        log.info("장바구니 다수 상품 결제 요청 수신: {}", payRequestDTO);
        // 인증된 사용자 ID를 PayRequestDTO에 설정
        payRequestDTO.setCustomerId(userDetails.getId());
        Long orderId = cartService.processCartPayment(payRequestDTO);
        log.info("장바구니를 통한 주문이 성공적으로 생성되었습니다. 주문 ID: {}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED); // 201 Created
    }
}