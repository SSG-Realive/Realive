package com.realive.repository.order; // 실제 패키지 경로

import com.realive.domain.order.Order;
// import com.realive.domain.product.Product; // findTopOrderedProducts를 다시 추가한다면 필요
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 이전 코드에 있었으나 현재 코드에는 없음. 필요시 추가.
import org.springframework.data.jpa.repository.Modifying; // 추가
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // 추가
import org.springframework.stereotype.Repository;

import java.util.List; // 추가
import java.util.Optional;

@Repository
// JpaSpecificationExecutor는 이전 코드에는 있었으나 현재 코드에는 없으므로 일단 제외. 필요시 추가.
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 고객 ID와 주문 ID로 특정 주문 조회 (기존 코드)
    Optional<Order> findByCustomer_IdAndId(Long customerId, Long id);

    // 모든 주문을 페이징하여 조회 (기존 코드)
    // OrderedAt 필드가 Order 엔티티에 있다고 가정
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer ORDER BY o.orderedAt DESC", // 필드명 'OrderedAt' -> 'orderedAt' (자바 네이밍 컨벤션)
            countQuery = "SELECT count(o) FROM Order o")
    Page<Order> findAllOrders(Pageable pageable);


    // === 아래는 "고객 삭제 시 연관된 주문 삭제" 기능을 위해 추가 제안하는 메소드들 ===

    /**
     * 특정 고객 ID에 해당하는 모든 주문 목록을 조회합니다.
     * Order 엔티티에 Customer 객체를 참조하는 'customer' 필드가 있다고 가정합니다.
     * @param customerId 고객 ID
     * @return 해당 고객의 주문 목록
     */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId")
    List<Order> findAllByCustomerId(@Param("customerId") Long customerId);

    /**
     * 특정 고객 ID에 해당하는 모든 주문을 데이터베이스에서 직접 삭제합니다.
     * 이 메소드는 연결된 OrderDelivery 등의 자식 데이터는 자동으로 처리하지 않으므로,
     * 서비스 계층에서 OrderDelivery 삭제 후 이 메소드를 호출하거나,
     * 또는 Order 엔티티에 CascadeType.REMOVE 설정이 되어 있어야 합니다.
     * (현재 "엔티티 수정 없이" 원칙이므로 Cascade 설정은 사용하지 않음)
     * @param customerId 고객 ID
     * @return 삭제된 주문의 수 (선택적 반환 타입, void도 가능)
     */
    @Modifying // 데이터를 변경하는 쿼리임을 명시
    @Query("DELETE FROM Order o WHERE o.customer.id = :customerId")
    int deleteByCustomerId(@Param("customerId") Long customerId);


    // === 이전에 보여주셨던 코드에 있던 유용한 메소드들 (필요시 다시 추가 가능) ===
    /*
    Page<Order> findByProductId(Long productId, Pageable pageable);

    long countByProductId(Long productId);

    @Query("SELECT o FROM Order o WHERE o.product.seller.id = :sellerId")
    Page<Order> findByProductSellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT COUNT(o.id) FROM Order o WHERE o.product.seller.id = :sellerId")
    long countByProductSellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT o.product FROM Order o GROUP BY o.product.id ORDER BY COUNT(o.product.id) DESC")
    List<Product> findTopOrderedProducts(Pageable pageable);
    */
}
