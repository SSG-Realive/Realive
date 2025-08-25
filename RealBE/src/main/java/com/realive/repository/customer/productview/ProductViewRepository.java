package com.realive.repository.customer.productview;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realive.domain.product.Product;

// [Customer] 상품 찾기 Repository
public interface ProductViewRepository extends JpaRepository<Product, Long>, ProductSearch, ProductDetail {

    // 상품ID로 상품 찾기
    Optional<Product> findById(Long id);

    // ✅ 카테고리별 인기 상품 (찜 많은 순)
    @Query(value = """
SELECT p.id, p.name, p.price, COUNT(w.id) AS wishCount
FROM products p
JOIN wishlists w ON p.id = w.product_id
WHERE p.category_id IN (:categoryIds)
GROUP BY p.id
ORDER BY wishCount DESC
LIMIT 15
""", nativeQuery = true)
    List<Object[]> findPopularProductRawByCategoryIds(@Param("categoryIds") List<Long> categoryIds);


}
