package com.realive.repository.cart;

import com.realive.domain.customer.CartItem;
import com.realive.domain.customer.Customer;
import com.realive.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long>, CartItemRepositoryCustom {
    // 특정 고객의 장바구니 항목들을 모두 조회 (JPA 기본 기능)
    // findByCustomer_Id는 customer 엔티티의 id 필드를 통해 조회
    List<CartItem> findByCustomer_Id(Long customerId);

    // 특정 고객의 특정 상품이 장바구니에 있는지 확인 (JPA 기본 기능)
    // findByCustomer_IdAndProduct_Id는 customer 엔티티의 id 필드와 product 엔티티의 id 필드를 통해 조회
    Optional<CartItem> findByCustomer_IdAndProduct_Id(Long customerId, Long productId);

    // DELETE 쿼리는 Querydsl로 처리하기 어려움 (벌크 연산). JPA @Modifying @Query를 유지하는 것이 좋습니다.
    // 또는 EntityManager를 사용하여 직접 벌크 삭제를 구현할 수 있습니다.
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.customer.id = :customerId") // customer.id로 변경
    void deleteByCustomer_Id(@Param("customerId") Long customerId);

    // CartItem ID와 Customer ID로 항목 조회 (JPA 기본 기능)
    Optional<CartItem> findByIdAndCustomer_Id(Long cartItemId, Long customerId);
}