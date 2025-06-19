package com.realive.repository.product;

import com.realive.domain.product.Product;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 상품 정보를 DB에서 조회/저장/삭제하는 JPA Repository
 */
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    /**
     * 판매자 ID로 상품 목록 조회
     * 
     * @param sellerId 판매자 ID
     * @return 해당 판매자의 상품 리스트
     */
    List<Product> findBySellerId(Long sellerId);

    // 오늘 등록된 신규 상품 수 조회 (BaseTimeEntity의 createdAt 필드 사용 가정)
    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdAt >= :startOfDay AND p.createdAt < :endOfDay")
    long countByCreatedAtBetween(@Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    List<Product> findAllByIdIn(List<Long> ids); // 여러 ID로 조회

    // 등록된 상품 수 (isActive = true)
    long countBySellerIdAndActiveTrue(Long sellerId);

    // 오늘 등록된 상품 수
    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId AND p.createdAt BETWEEN :startOfDay AND :endOfDay")
    long countTodayProductBySellerId(@Param("sellerId") Long sellerId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // 🚩 PESSIMISTIC LOCK 재고 차감용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Product findByIdForUpdate(@Param("productId") Long productId);

    // 관련 상품 추천
    List<Product> findTop6ByCategoryIdAndIdNotAndActiveTrue(Long categoryId, Long excludeProductId);
}

