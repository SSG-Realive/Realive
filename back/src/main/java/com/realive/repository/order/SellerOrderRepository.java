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

    // 특정 상품의 주문 목록 (상품 ID → OrderItem → Order)
    @Query("""
        SELECT DISTINCT oi.order
        FROM OrderItem oi
        WHERE oi.product.id = :productId
    """)
    Page<Order> findByProductId(@Param("productId") Long productId, Pageable pageable);

    // 특정 상품의 주문 수 (카운트)
    @Query("""
        SELECT COUNT(DISTINCT oi.order.id)
        FROM OrderItem oi
        WHERE oi.product.id = :productId
    """)
    long countByProductId(@Param("productId") Long productId);

    // 특정 판매자의 주문 목록
    @Query("""
        SELECT DISTINCT oi.order
        FROM OrderItem oi
        WHERE oi.product.seller.id = :sellerId
    """)
    Page<Order> findByProductSellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    // 특정 판매자의 주문 수
    @Query("""
        SELECT COUNT(DISTINCT oi.order.id)
        FROM OrderItem oi
        WHERE oi.product.seller.id = :sellerId
    """)
    long countByProductSellerId(@Param("sellerId") Long sellerId);

    // 가장 많이 팔린 상품 TOP N
    @Query("""
        SELECT oi.product
        FROM OrderItem oi
        GROUP BY oi.product.id
        ORDER BY COUNT(oi.product.id) DESC
    """)
    List<Product> findTopOrderedProducts(Pageable pageable);
}
