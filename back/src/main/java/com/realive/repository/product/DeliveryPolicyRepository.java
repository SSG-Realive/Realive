package com.realive.repository.product;

import com.realive.domain.product.DeliveryPolicy;
import com.realive.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 배송 정책 관련 Repository
 */

public interface DeliveryPolicyRepository extends JpaRepository<DeliveryPolicy, Long> {
    Optional<DeliveryPolicy> findByProduct(Product product);

    // 필요시 커스텀 메서드 추가 가능
}