package com.realive.service.cart.crud;

import com.realive.domain.customer.CartItem;
import com.realive.domain.customer.Customer;
import com.realive.domain.product.Product; // Product 엔티티는 여전히 필요 (재고, 활성화 여부 등 체크용)
import com.realive.dto.cart.CartItemAddRequestDTO;
import com.realive.dto.cart.CartItemResponseDTO;
import com.realive.dto.cart.CartItemUpdateRequestDTO;
import com.realive.dto.productview.ProductResponseDto; // ProductResponseDto 임포트
import com.realive.repository.cart.CartItemRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.product.ProductRepository; // Product 엔티티 조회를 위해 유지
import com.realive.repository.productview.ProductViewRepository; // ProductViewRepository 임포트
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
    private final ProductRepository productRepository; // Product 엔티티의 재고 등을 직접 조회하기 위해 유지
    private final ProductViewRepository productViewRepository; // 상품 상세 정보를 DTO로 가져오기 위해 사용
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CartItemResponseDTO addCartItem(Long customerId, CartItemAddRequestDTO requestDTO) {
        Long productId = requestDTO.getProductId();
        int quantityToAdd = requestDTO.getQuantity();

        customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + customerId));

        // 재고 확인을 위해 Product 엔티티 자체는 여전히 필요 (ProductViewRepository는 DTO를 반환하므로)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));

        if (product.getStock() < quantityToAdd) {
            throw new IllegalArgumentException("Not enough stock for product ID: " + productId);
        }
        // TODO: 상품 상태 (isActive, status) 확인 로직 추가

        Optional<CartItem> existingCartItem = cartItemRepository.findByCustomerIdAndProductId(customerId, productId);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantityToAdd;
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("Not enough stock to update quantity for product ID: " + productId);
            }
            cartItem.setQuantity(newQuantity);
            log.info("Updating quantity for existing cart item: {}", cartItem.getId());
        } else {
            cartItem = CartItem.builder()
                    .customerId(customerId)
                    .productId(productId)
                    .quantity(quantityToAdd)
                    .build();
            log.info("Adding new cart item for customer {} and product {}: Quantity = {}", customerId, productId, quantityToAdd);
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        // ProductViewRepository를 통해 상세 DTO 조회
        ProductResponseDto productDetailDto = productViewRepository.findProductDetailById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product detail not found for ID: " + productId));

        return CartItemResponseDTO.from(savedCartItem, productDetailDto);
    }

    @Override
    @Transactional
    public CartItemResponseDTO updateCartItemQuantity(Long customerId, Long cartItemId, CartItemUpdateRequestDTO requestDTO) {
        int newQuantity = requestDTO.getQuantity();

        CartItem cartItem = cartItemRepository.findByIdAndCustomerId(cartItemId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found or unauthorized with ID: " + cartItemId));

        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("Removed cart item {} as quantity became 0 or less.", cartItemId);
            return null;
        }

        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product associated with cart item not found."));

        if (product.getStock() < newQuantity) {
            throw new IllegalArgumentException("Not enough stock for product ID: " + product.getId() + " to update quantity to " + newQuantity);
        }
        // TODO: 상품 상태 (isActive, status) 확인 로직 추가

        cartItem.setQuantity(newQuantity);
        log.info("Updated cart item {} quantity to {}", cartItemId, newQuantity);

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        ProductResponseDto productDetailDto = productViewRepository.findProductDetailById(cartItem.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product detail not found for ID: " + cartItem.getProductId()));

        return CartItemResponseDTO.from(updatedCartItem, productDetailDto);
    }

    @Override
    @Transactional
    public void removeCartItem(Long customerId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndCustomerId(cartItemId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found or unauthorized with ID: " + cartItemId));

        cartItemRepository.delete(cartItem);
        log.info("Removed cart item: {}", cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomerId(customerId);
        log.info("Cleared cart for customer: {}", customerId);
    }
}