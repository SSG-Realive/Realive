package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.logs.SalesLog;
import com.realive.domain.order.Order;
import com.realive.dto.admin.management.OrderDTO;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.logs.SalesLogRepository;
import com.realive.service.admin.logs.StatService;
import com.realive.service.admin.management.service.OrderManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderManagementServiceImpl implements OrderManagementService {

    private final OrderRepository orderRepository;
    private final SalesLogRepository salesLogRepository;
    private final StatService statService;

    @Override
    public Page<OrderDTO> getOrders(Pageable pageable) {
        Page<SalesLog> salesLogsPage = salesLogRepository.findAll(pageable);
        List<OrderDTO> orderDTOs = salesLogsPage.getContent().stream()
                .map(this::convertSalesLogToOrderDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(orderDTOs, pageable, salesLogsPage.getTotalElements());
    }

    @Override
    public Page<OrderDTO> searchOrders(Map<String, Object> searchParams, Pageable pageable) {
        Specification<SalesLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchParams.get("customerId") != null && searchParams.get("customerId").toString().matches("\\d+")) {
                predicates.add(cb.equal(root.get("customerId"), Integer.parseInt(searchParams.get("customerId").toString())));
            }
            if (searchParams.get("sellerId") != null && searchParams.get("sellerId").toString().matches("\\d+")) {
                predicates.add(cb.equal(root.get("sellerId"), Integer.parseInt(searchParams.get("sellerId").toString())));
            }
            if (searchParams.get("productId") != null && searchParams.get("productId").toString().matches("\\d+")) {
                predicates.add(cb.equal(root.get("productId"), Integer.parseInt(searchParams.get("productId").toString())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SalesLog> salesLogsPage = salesLogRepository.findAll(spec, pageable);
        List<OrderDTO> orderDTOs = salesLogsPage.getContent().stream()
                .map(this::convertSalesLogToOrderDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(orderDTOs, pageable, salesLogsPage.getTotalElements());
    }

    @Override
    public OrderDTO getOrderById(Integer orderId) {
        Optional<SalesLog> salesLogOptional = salesLogRepository.findById(orderId);
        SalesLog salesLog = salesLogOptional.orElseThrow(() -> new NoSuchElementException("SalesLog 없음 (주문 상세 조회용) ID: " + orderId));
        return convertSalesLogToOrderDTO(salesLog);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Integer orderId, String status) {
        log.warn("Order 엔티티의 필드 제약으로 인해 주문 상태 직접 변경은 지원되지 않습니다. SalesLog ID: {}", orderId);
        Optional<SalesLog> salesLogOptional = salesLogRepository.findById(orderId);
        SalesLog salesLog = salesLogOptional.orElseThrow(() -> new NoSuchElementException("SalesLog 없음 (상태 변경 시도) ID: " + orderId));
        return convertSalesLogToOrderDTO(salesLog);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Integer orderId, String reason) {
        log.warn("Order 엔티티의 필드 제약으로 인해 주문 취소는 지원되지 않습니다. SalesLog ID: {}", orderId);
        Optional<SalesLog> salesLogOptional = salesLogRepository.findById(orderId);
        SalesLog salesLog = salesLogOptional.orElseThrow(() -> new NoSuchElementException("SalesLog 없음 (취소 시도) ID: " + orderId));
        return convertSalesLogToOrderDTO(salesLog);
    }

    @Override
    public Map<String, Object> getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        Long totalOrders = salesLogRepository.countDistinctOrdersBySoldAtBetween(startDate, endDate);
        Integer totalSalesAmount = salesLogRepository.sumTotalPriceBySoldAtBetween(startDate, endDate);

        stats.put("totalOrdersInPeriod", totalOrders != null ? totalOrders : 0L);
        stats.put("totalSalesInPeriod", totalSalesAmount != null ? BigDecimal.valueOf(totalSalesAmount) : BigDecimal.ZERO);
        return stats;
    }

    @Override
    public Page<OrderDTO> getCustomerOrders(Integer customerId, Pageable pageable) {
        Page<SalesLog> salesLogsPage = salesLogRepository.findByCustomerId(customerId, pageable);
        List<OrderDTO> orderDTOs = salesLogsPage.getContent().stream()
                .map(this::convertSalesLogToOrderDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(orderDTOs, pageable, salesLogsPage.getTotalElements());
    }

    @Override
    public Page<OrderDTO> getSellerOrders(Integer sellerId, Pageable pageable) {
        Page<SalesLog> salesLogsPage = salesLogRepository.findBySellerId(sellerId, pageable);
        List<OrderDTO> orderDTOs = salesLogsPage.getContent().stream()
                .map(this::convertSalesLogToOrderDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(orderDTOs, pageable, salesLogsPage.getTotalElements());
    }

    private OrderDTO convertToOrderDTO(Order e) {
        if (e == null) return null;
        return OrderDTO.builder()
                .id(e.getId() != null ? e.getId().intValue() : null)
                .build();
    }

    private OrderDTO convertSalesLogToOrderDTO(SalesLog sl) {
        if (sl == null) return null;
        return OrderDTO.builder()
                .id(sl.getOrderItemId() != null ? sl.getOrderItemId() : sl.getId())
                .customerId(sl.getCustomerId())
                .orderDate(sl.getSoldAt() != null ? sl.getSoldAt().atStartOfDay() : null)
                .totalAmount(sl.getTotalPrice())
                .status("COMPLETED")
                .build();
    }
}
