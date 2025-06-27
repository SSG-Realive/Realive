package com.realive.repository.customer;

import java.util.List;
import java.util.Optional;

import com.realive.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realive.domain.customer.Wishlist;

// [Customer] 찜 Repository

public interface WishlistRepository extends JpaRepository<Wishlist, Long>  {

    // 고객ID, 상품ID로 찜/찜삭제
    @Query("SELECT w FROM Wishlist w WHERE w.customer.id = :customerId AND w.product.id = :productId")
    Optional<Wishlist> findByCustomerIdAndProductId(@Param("customerId") Long customerId,@Param("productId") Long productId);

    // 고객ID로 찜 목록 조회용 상품ID 뽑기
    @Query("SELECT w.product.id FROM Wishlist w WHERE w.customer.id = :customerId ORDER BY w.created DESC")
    List<Long> findProductIdsByCustomerId(@Param("customerId") Long customerId);

    // 인기 상품 (찜 많은 순) 상위 6개
    @Query("""
    SELECT w.product 
    FROM Wishlist w
    WHERE w.product.active = true
    GROUP BY w.product
    ORDER BY COUNT(w.id) DESC
""")
    List<Product> findTop6PopularProducts();

    @Query("""
        SELECT p
        FROM Wishlist w
        JOIN w.product p
        JOIN FETCH p.category c
        LEFT JOIN FETCH c.parent
        WHERE w.customer.id = :customerId
        ORDER BY w.created DESC
    """)
        List<Product> findWishlistProductsWithCategory(@Param("customerId") Long customerId);


}
