package com.realive.repository.cart;

import com.realive.domain.customer.CartItem;
import com.realive.dto.cart.CartItemResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT new com.realive.dto.cart.CartItemResponseDTO" +
            "(c.cartId, c.product.id, c.product.name, c.quantity, c.product.price, c.product.imageUrl, c.quantity * c.product.price, c.created) " +
            "FROM CartItem c WHERE c.cartId = :cartId AND c.customer.id = :customerId")
    Optional<CartItemResponseDTO> findByCartIdAndCustomerId(Long cartId, Long customerId); // 장바구니 단일 상품 조회

    @Query("SELECT new com.realive.dto.cart.CartItemResponseDTO(c.cartId, c.product.id, c.product.name, c.quantity, c.product.price, c.product.imageUrl, c.quantity * c.product.price, c.created) " +
            "FROM CartItem c WHERE c.customer.id = :customerId AND (:lastCreated IS NULL OR c.created < :lastCreated) " +
            "ORDER BY c.created DESC LIMIT :size")
    List<CartItemResponseDTO> findByCustomerIdForInfiniteScroll(Long customerId, LocalDateTime lastCreated, int size); // 장바구니 조회 + 무한스크롤

    void deleteByCartIdAndCustomerId(Long cartId, Long customerId); // 삭제

}
