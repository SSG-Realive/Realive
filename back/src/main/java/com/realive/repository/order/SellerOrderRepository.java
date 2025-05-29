package com.realive.repository.order;

import com.realive.domain.order.Order;
import com.realive.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerOrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    // ❌ 잘못된 접근 방식 → 삭제 권장
    // Page<Order> findByProductId(Long productId, Pageable pageable);
    // long countByProductId(Long productId);

    /**
     * ✅ 판매자 ID 기준으로 주문 조회 (Order → OrderItem → Product)
     */
    @Query("""
        SELECT DISTINCT o FROM Order o
        JOIN o.orderItems i
        JOIN i.product p
        WHERE p.seller.id = :sellerId
    """)
    Page<Order> findByProductSellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    /**
     * ✅ 판매자 주문 수 카운트
     */
    @Query("""
        SELECT COUNT(DISTINCT o.id) FROM Order o
        JOIN o.orderItems i
        JOIN i.product p
        WHERE p.seller.id = :sellerId
    """)
    long countByProductSellerId(@Param("sellerId") Long sellerId);

    /**
     * ✅ 가장 많이 주문된 상품 조회 (상위 N개)
     */
    @Query("""
        SELECT i.product FROM OrderItem i
        GROUP BY i.product.id
        ORDER BY COUNT(i.product.id) DESC
    """)
    List<Product> findTopOrderedProducts(Pageable pageable);
}