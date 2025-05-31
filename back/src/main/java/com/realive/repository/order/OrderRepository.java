package com.realive.repository.order;

import com.realive.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCustomer_IdAndId(Long customerId, Long id);

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer ORDER BY o.OrderedAt DESC",
            countQuery = "SELECT count(o) FROM Order o")
    Page<Order> findAllOrders(Pageable pageable);

    /**
     * 특정 고객 ID에 해당하는 모든 주문 목록을 조회합니다.
     * 사용자가 비활성화될 때 관련 주문들의 상태를 변경하기 위해 사용됩니다.
     *
     * @param customerId 조회할 고객의 ID
     * @return 해당 고객의 모든 주문 리스트
     */
    List<Order> findAllByCustomerId(Long customerId);
}