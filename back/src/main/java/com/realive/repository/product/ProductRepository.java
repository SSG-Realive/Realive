package com.realive.repository.product;

import com.realive.domain.product.DeliveryPolicy;
import com.realive.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 상품 정보를 DB에서 조회/저장/삭제하는 JPA Repository
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 판매자 ID로 상품 목록 조회
     * @param sellerId 판매자 ID
     * @return 해당 판매자의 상품 리스트
     */
    List<Product> findBySellerId(Long sellerId);

    /**
     * 배송 정책 관련 Repository
     */
    interface DeliveryPolicyRepository extends JpaRepository<DeliveryPolicy, Long> {

        // 필요시 커스텀 메서드 추가 가능
    }
}