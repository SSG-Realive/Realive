package com.realive.repository.cart;

import com.realive.domain.customer.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long>, CartItemRepositoryCustom {

    List<CartItem> findByCustomer_Id(Long customerId);

    // 특정 고객의 특정 상품이 장바구니에 있는지 확인
    Optional<CartItem> findByCustomer_IdAndProduct_Id(Long customerId, Long productId);

    // 전체 삭제 기능
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.customer.id = :customerId")
    void deleteByCustomer_Id(@Param("customerId") Long customerId);

    // CartItem ID와 Customer ID로 항목 조회
    Optional<CartItem> findByIdAndCustomer_Id(Long cartItemId, Long customerId);
}