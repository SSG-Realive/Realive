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

    // Page<Order> findByProductId(Long productId, Pageable pageable);

    // long countByProductId(Long productId);

    // @Query("SELECT o FROM Order o WHERE o.product.seller.id = :sellerId")
    // Page<Order> findByProductSellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    // @Query("SELECT COUNT(o.id) FROM Order o WHERE o.product.seller.id = :sellerId")
    // long countByProductSellerId(@Param("sellerId") Long sellerId);

    // @Query("SELECT o.product FROM Order o GROUP BY o.product.id ORDER BY COUNT(o.product.id) DESC")
    // List<Product> findTopOrderedProducts(Pageable pageable);

}