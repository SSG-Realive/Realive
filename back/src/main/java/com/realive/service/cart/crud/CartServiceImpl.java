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
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + customerId));

        // Product 엔티티 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));

        if (product.getStock() < quantityToAdd) {
            throw new IllegalArgumentException("Not enough stock for product ID: " + productId);
        }
        // TODO: 상품 상태 (isActive, status) 확인 로직 추가
        if (!product.isActive() || product.getStatus() == null /*|| product.getStatus() == ProductStatus.하*/) { // 예시: ProductStatus.하인 경우 추가 불가
            throw new IllegalArgumentException("Product is not active or in an unpurchasable state for product ID: " + productId);
        }


        // 기존 장바구니 항목 조회 시 Customer 엔티티와 Product 엔티티를 직접 사용
        Optional<CartItem> existingCartItem = cartItemRepository.findByCustomer_IdAndProduct_Id(customerId, productId);

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
                    .customer(customer)
                    .product(product)
                    .quantity(quantityToAdd)
                    .build();
            log.info("Adding new cart item for customer {} and product {}: Quantity = {}", customerId, productId, quantityToAdd);
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        ProductResponseDTO productDetailDto = productViewRepository.findProductDetailById(savedCartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("Product detail not found for ID: " + savedCartItem.getProduct().getId()));

        return CartItemResponseDTO.from(savedCartItem, productDetailDto);
    }

    @Override
    @Transactional
    public CartItemResponseDTO updateCartItemQuantity(Long customerId, Long cartItemId, CartItemUpdateRequestDTO requestDTO) {
        int newQuantity = requestDTO.getQuantity();

        CartItem cartItem = cartItemRepository.findByIdAndCustomer_Id(cartItemId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found or unauthorized with ID: " + cartItemId));

        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("Removed cart item {} as quantity became 0 or less.", cartItemId);
            return null;
        }

        // Product 엔티티 조회
        Product product = productRepository.findById(cartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("Product associated with cart item not found."));

        if (product.getStock() < newQuantity) {
            throw new IllegalArgumentException("Not enough stock for product ID: " + product.getId() + " to update quantity to " + newQuantity);
        }
        // TODO: 상품 상태 (isActive, status) 확인 로직 추가
        if (!product.isActive() || product.getStatus() == null /*|| product.getStatus() == ProductStatus.하*/) { // 예시: ProductStatus.하인 경우 추가 불가
            throw new IllegalArgumentException("Product is not active or in an unpurchasable state for product ID: " + product.getId());
        }


        cartItem.setQuantity(newQuantity);
        log.info("Updated cart item {} quantity to {}", cartItemId, newQuantity);

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        // ProductViewRepository를 통해 상세 DTO 조회
        ProductResponseDTO productDetailDto = productViewRepository.findProductDetailById(updatedCartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("Product detail not found for ID: " + updatedCartItem.getProduct().getId()));

        return CartItemResponseDTO.from(updatedCartItem, productDetailDto);
    }

    @Override
    @Transactional
    public void removeCartItem(Long customerId, Long cartItemId) {
        // findByIdAndCustomer_Id로 수정
        CartItem cartItem = cartItemRepository.findByIdAndCustomer_Id(cartItemId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found or unauthorized with ID: " + cartItemId));

        cartItemRepository.delete(cartItem);
        log.info("Removed cart item: {}", cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomer_Id(customerId);
        log.info("Cleared cart for customer: {}", customerId);
    }
}