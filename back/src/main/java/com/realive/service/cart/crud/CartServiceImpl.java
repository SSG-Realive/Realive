package com.realive.service.cart.crud;

import com.realive.domain.customer.CartItem;
import com.realive.domain.customer.Customer;
import com.realive.domain.product.Product;
import com.realive.dto.cart.CartItemAddRequestDTO;
import com.realive.dto.cart.CartItemResponseDTO;
import com.realive.dto.cart.CartItemUpdateRequestDTO;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.repository.cart.CartItemRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.customer.productview.ProductViewRepository;
import com.realive.repository.product.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductViewRepository productViewRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CartItemResponseDTO addCartItem(Long customerId, CartItemAddRequestDTO requestDTO) {
        Long productId = requestDTO.getProductId();
        int quantityToAdd = requestDTO.getQuantity();

        // Customer 엔티티 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + customerId + "번 고객을 찾을 수 없습니다."));

        // Product 엔티티 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + productId + "번 상품을 찾을 수 없습니다."));

        if (product.getStock() < quantityToAdd) {
            throw new IllegalArgumentException("상품 ID " + productId + "번의 재고가 부족합니다.");
        }
        // TODO: 상품 상태 (isActive, status) 확인 로직 추가
        if (!product.isActive() || product.getStatus() == null /*|| product.getStatus() == ProductStatus.하*/) { // 예시: ProductStatus.하인 경우 추가 불가
            throw new IllegalArgumentException("상품 ID " + productId + "번이 활성화되어 있지 않거나 구매할 수 없는 상태입니다.");
        }

        // 기존 장바구니 항목 조회 시 Customer 엔티티와 Product 엔티티를 직접 사용
        Optional<CartItem> existingCartItem = cartItemRepository.findByCustomer_IdAndProduct_Id(customerId, productId);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantityToAdd;
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("상품 ID " + productId + "번의 재고가 부족하여 수량을 업데이트할 수 없습니다.");
            }
            cartItem.setQuantity(newQuantity);
            log.info("기존 장바구니 항목 ID {}의 수량을 업데이트합니다.", cartItem.getId());
        } else {
            cartItem = CartItem.builder()
                    .customer(customer)
                    .product(product)
                    .quantity(quantityToAdd)
                    .build();
            log.info("고객 ID {}와 상품 ID {}에 대한 새로운 장바구니 항목을 추가합니다. 수량: {}", customerId, productId, quantityToAdd);
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        ProductResponseDTO productDetailDto = productViewRepository.findProductDetailById(savedCartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("ID " + savedCartItem.getProduct().getId() + "번 상품의 상세 정보를 찾을 수 없습니다."));

        return CartItemResponseDTO.from(savedCartItem, productDetailDto);
    }

    @Override
    @Transactional
    public CartItemResponseDTO updateCartItemQuantity(Long customerId, Long cartItemId, CartItemUpdateRequestDTO requestDTO) {
        int newQuantity = requestDTO.getQuantity();

        CartItem cartItem = cartItemRepository.findByIdAndCustomer_Id(cartItemId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + cartItemId + "번 장바구니 항목을 찾을 수 없거나 권한이 없습니다."));

        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("수량이 0 이하가 되어 장바구니 항목 ID {}를 제거했습니다.", cartItemId);
            return null; // 장바구니 항목 제거 시 null 반환
        }

        // Product 엔티티 조회
        Product product = productRepository.findById(cartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("장바구니 항목과 연결된 상품을 찾을 수 없습니다."));

        if (product.getStock() < newQuantity) {
            throw new IllegalArgumentException("상품 ID " + product.getId() + "번의 재고가 부족하여 수량을 " + newQuantity + "(으)로 업데이트할 수 없습니다.");
        }
        // TODO: 상품 상태 (isActive, status) 확인 로직 추가
        if (!product.isActive() || product.getStatus() == null /*|| product.getStatus() == ProductStatus.하*/) { // 예시: ProductStatus.하인 경우 추가 불가
            throw new IllegalArgumentException("상품 ID " + product.getId() + "번이 활성화되어 있지 않거나 구매할 수 없는 상태입니다.");
        }

        cartItem.setQuantity(newQuantity);
        log.info("장바구니 항목 ID {}의 수량을 {}로 업데이트했습니다.", cartItemId, newQuantity);

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        // ProductViewRepository를 통해 상세 DTO 조회
        ProductResponseDTO productDetailDto = productViewRepository.findProductDetailById(updatedCartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("ID " + updatedCartItem.getProduct().getId() + "번 상품의 상세 정보를 찾을 수 없습니다."));

        return CartItemResponseDTO.from(updatedCartItem, productDetailDto);
    }

    @Override
    @Transactional
    public void removeCartItem(Long customerId, Long cartItemId) {
        // findByIdAndCustomer_Id로 수정
        CartItem cartItem = cartItemRepository.findByIdAndCustomer_Id(cartItemId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + cartItemId + "번 장바구니 항목을 찾을 수 없거나 권한이 없습니다."));

        cartItemRepository.delete(cartItem);
        log.info("장바구니 항목 ID {}를 제거했습니다.", cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomer_Id(customerId);
        log.info("고객 ID {}의 장바구니를 비웠습니다.", customerId);
    }
}