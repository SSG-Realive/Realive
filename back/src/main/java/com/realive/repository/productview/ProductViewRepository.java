package com.realive.repository.productview;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realive.domain.product.Product;
import com.realive.dto.productview.ProductResponseDto;

public interface ProductViewRepository extends JpaRepository<Product, Long>, ProductSearch, ProductDetail {
    // JpaRepository 기본 CRUD + ProductSearch 커스텀 검색 메서드 같이 사용
    // @Query("select new com.realive.dto.product.ProductResponseDto(p.id, p.name, p.description, p.price, p.stock,p.width, p.depth, p.height,p.status, p.isActive,pi.url, c.name, s.name)FROM Product p LEFT JOIN p.url pi ON pi.isThumbnail = true LEFT JOIN p.category c LEFT JOIN p.seller s WHERE p.id = :id")
    // Optional<ProductResponseDto> findProductDetailById(@Param("id") Long id);

}