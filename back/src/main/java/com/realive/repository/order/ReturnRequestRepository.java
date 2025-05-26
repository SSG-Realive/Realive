package com.realive.repository.order;

import com.realive.domain.order.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    // 특정 주문에 대한 반품 요청 조회
    List<ReturnRequest> findByOrderId(Long orderId);

    // 특정 고객의 특정 반품 요청 조회 (보안을 위해 customerId로 필터링)
    Optional<ReturnRequest> findByIdAndOrder_Customer_Id(Long returnRequestId, Long customerId);

    // 특정 고객의 모든 반품 요청 조회
    List<ReturnRequest> findByOrder_Customer_Id(Long customerId);
}