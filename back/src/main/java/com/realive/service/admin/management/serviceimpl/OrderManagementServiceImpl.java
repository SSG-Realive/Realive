package com.realive.service.admin.management.serviceimpl;


import com.realive.dto.admin.management.OrderDTO;
import com.realive.repository.OrderRepository;
import com.realive.service.admin.management.orderman.OrderManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderManagementServiceImpl implements OrderManagementService {

    private final OrderRepository orderRepository;

    @Override
    public Page<OrderDTO> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<OrderDTO> searchOrders(Map<String, Object> searchParams, Pageable pageable) {
        Specification<Order> spec = Specification.where(null);

        if (searchParams.containsKey("status")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), searchParams.get("status")));
        }

        if (searchParams.containsKey("startDate") && searchParams.containsKey("endDate")) {
            LocalDate startDate = (LocalDate) searchParams.get("startDate");
            LocalDate endDate = (LocalDate) searchParams.get("endDate");
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("orderDate"), startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()));
        }

        if (searchParams.containsKey("customerId")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("customer").get("id"), searchParams.get("customerId")));
        }

        if (searchParams.containsKey("sellerId")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("items").get("product").get("seller").get("id"), searchParams.get("sellerId")));
        }

        return orderRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public OrderDTO getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new NoSuchElementException("주문 ID가 존재하지 않습니다: " + orderId));
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Integer orderId, String status) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문 ID가 존재하지 않습니다: " + orderId));

        order.setStatus(status);
        return convertToDTO(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Integer orderId, String reason) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문 ID가 존재하지 않습니다: " + orderId));

        // 주문 취소 로직 구현
        order.setStatus("CANCELLED");
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());

        // 재고 원복 등의 추가 로직이 필요할 수 있음

        return convertToDTO(orderRepository.save(order));
    }

    @Override
    public Map<String, Object> getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        // 통계 데이터 수집
        Long totalOrders = orderRepository.countByOrderDateBetween(
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        Integer totalSales = orderRepository.sumTotalAmountByOrderDateBetween(
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        Map<String, Long> ordersByStatus = orderRepository.countByStatusAndOrderDateBetween(
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        // 일별 매출 통계
        Map<LocalDate, Integer> dailySales = orderRepository.getDailySalesBetween(
                startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalOrders", totalOrders);
        statistics.put("totalSales", totalSales != null ? totalSales : 0);
        statistics.put("ordersByStatus", ordersByStatus);
        statistics.put("dailySales", dailySales);

        return statistics;
    }

    @Override
    public Page<OrderDTO> getCustomerOrders(Integer customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<OrderDTO> getSellerOrders(Integer sellerId, Pageable pageable) {
        return orderRepository.findBySellerId(sellerId, pageable)
                .map(this::convertToDTO);
    }

    // 엔티티 -> DTO 변환 메소드
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getName());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        return dto;
    }
}