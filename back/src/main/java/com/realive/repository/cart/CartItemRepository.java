package com.realive.repository.cart;

import com.realive.domain.customer.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // DELETE 쿼리에는 필요
import org.springframework.data.jpa.repository.Query; // DELETE 쿼리에는 필요
import org.springframework.data.repository.query.Param; // DELETE 쿼리에는 필요

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long>, CartItemRepositoryCustom {
    // 특정 고객의 장바구니 항목들을 모두 조회 (JPA 기본 기능)
    List<CartItem> findByCustomerId(Long customerId);

    // 특정 고객의 특정 상품이 장바구니에 있는지 확인 (JPA 기본 기능)
    Optional<CartItem> findByCustomerIdAndProductId(Long customerId, Long productId);

    // DELETE 쿼리는 Querydsl로 처리하기 어려움 (벌크 연산). JPA @Modifying @Query를 유지하는 것이 좋습니다.
    // 또는 EntityManager를 사용하여 직접 벌크 삭제를 구현할 수 있습니다.
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.customerId = :customerId")
    void deleteByCustomerId(@Param("customerId") Long customerId);

    // CartItem ID와 Customer ID로 항목 조회 (JPA 기본 기능)
    Optional<CartItem> findByIdAndCustomerId(Long cartItemId, Long customerId);
}