//package com.realive.service.admin.management.serviceimpl;
//
//// import com.realive.domain.order.Order; // Order 엔티티는 거의 사용 안 함
//import com.realive.domain.logs.SalesLog; // SalesLog 엔티티 경로
//import com.realive.dto.admin.management.OrderDTO;
//// import com.realive.repository.order.OrderRepository; // 직접 사용 최소화
//import com.realive.repository.logs.SalesLogRepository; // SalesLogRepository 주입
//import com.realive.service.admin.logs.StatService; // StatService 주입
//import com.realive.service.admin.management.service.OrderManagementService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import jakarta.persistence.criteria.Predicate;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.NoSuchElementException; // 추가
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class OrderManagementServiceImpl implements OrderManagementService {
//
//    // private final OrderRepository orderRepository; // Order 정보가 제한적이므로 사용 최소화
//    private final SalesLogRepository salesLogRepository; // 실제 판매/결제된 내역 기반
//    private final StatService statService;
//
//    @Override
//    public Page<OrderDTO> getOrders(Pageable pageable) {
//        log.info("주문 목록 조회 (SalesLog 기반) - page: {}", pageable.getPageNumber());
//        // SalesLogRepository에 페이징 지원하는 findAll 메소드 필요
//        Page<SalesLog> salesLogsPage = salesLogRepository.findAllSalesLogs(pageable); // 예시 메소드명
//        List<OrderDTO> orderDTOs = salesLogsPage.getContent().stream()
//                .map(this::convertSalesLogToOrderDTO)
//                .collect(Collectors.toList());
//        return new PageImpl<>(orderDTOs, pageable, salesLogsPage.getTotalElements());
//    }
//
//    @Override
//    public Page<OrderDTO> searchOrders(Map<String, Object> searchParams, Pageable pageable) {
//        log.info("주문 검색 (SalesLog 기반) - params: {}", searchParams);
//        // SalesLogRepository에 Specification을 사용하여 검색하는 로직 구현
//        Specification<SalesLog> spec = (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//            // SalesLog 엔티티의 필드(customerId, sellerId, productId, soldAt 등)로 검색 조건 구성
//            if (searchParams.get("customerId") != null) {
//                predicates.add(cb.equal(root.get("customerId"), Long.parseLong(searchParams.get("customerId").toString())));
//            }
//            if (searchParams.get("sellerId") != null) {
//                predicates.add(cb.equal(root.get("sellerId"), Long.parseLong(searchParams.get("sellerId").toString())));
//            }
//            // ... 기타 검색 조건
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//        Page<SalesLog> salesLogsPage = salesLogRepository.findAll(spec, pageable);
//        List<OrderDTO> orderDTOs = salesLogsPage.getContent().stream()
//                .map(this::convertSalesLogToOrderDTO)
//                .collect(Collectors.toList());
//        return new PageImpl<>(orderDTOs, pageable, salesLogsPage.getTotalElements());
//    }
//
//    @Override
//    public OrderDTO getOrderById(Integer orderId) {
//        log.info("주문 상세 조회 (SalesLog 기반, orderItemId 또는 SalesLog ID 기준) - ID: {}", orderId);
//        // SalesLogRepository에 findByOrderItemId 또는 findById(Integer id) 메소드 필요
//        // SalesLog ID를 주문 ID로 간주하는 경우
//        SalesLog salesLog = salesLogRepository.findById(orderId) // SalesLog PK가 Integer라고 가정
//                .orElseThrow(() -> new NoSuchElementException("SalesLog 없음 ID: " + orderId));
//        return convertSalesLogToOrderDTO(salesLog);
//    }
//
//    @Override
//    @Transactional
//    public OrderDTO updateOrderStatus(Integer orderId, String status) {
//        log.warn("주문 상태 업데이트는 SalesLog가 아닌 별도 주문 상태 관리 시스템 또는 Order 엔티티 수정 필요. SalesLog ID: {}", orderId);
//        // SalesLog는 보통 불변의 기록이므로 상태 변경 대상이 아님.
//        // Order 엔티티가 상태를 가졌다면 그곳에서 처리.
//        SalesLog salesLog = salesLogRepository.findById(orderId)
//                .orElseThrow(() -> new NoSuchElementException("SalesLog 없음 ID: " + orderId));
//        return convertSalesLogToOrderDTO(salesLog); // 상태 변경 없음
//    }
//
//    @Override
//    @Transactional
//    public OrderDTO cancelOrder(Integer orderId, String reason) {
//        log.warn("주문 취소 처리는 SalesLog가 아닌 실제 주문 관리 시스템 및 Order 엔티티 수정 필요. SalesLog ID: {}", orderId);
//        // SalesLog는 판매 기록이므로 취소 시 별도 '취소 로그'를 생성하거나,
//        // 원본 SalesLog에 '취소됨' 상태를 추가하는 등의 처리 필요 (현재 SalesLog 엔티티 구조에 따라 다름)
//        SalesLog salesLog = salesLogRepository.findById(orderId)
//                .orElseThrow(() -> new NoSuchElementException("SalesLog 없음 ID: " + orderId));
//        // 예: salesLog.setStatus("CANCELLED"); salesLogRepository.save(salesLog); (SalesLog에 status 필드 필요)
//        return convertSalesLogToOrderDTO(salesLog); // 상태 변경 없음 (예시)
//    }
//
//    @Override
//    public Map<String, Object> getOrderStatistics(LocalDate startDate, LocalDate endDate) {
//        log.info("기간별 주문 통계 조회 (StatService 활용)");
//        Map<String, Object> stats = new HashMap<>();
//        // StatService에 해당 기간의 총 판매 건수(주문 단위), 총 판매액 집계 메소드 필요
//        // 예: DailySalesSummaryDTO summary = statService.getAggregatedSalesSummaryForPeriod(startDate, endDate);
//        // stats.put("totalOrdersInPeriod", summary.getTotalOrderCount());
//        // stats.put("totalSalesInPeriod", summary.getTotalSalesAmount());
//        log.warn("getOrderStatistics: StatService에 기간 집계 메소드 구현 및 호출 필요");
//        stats.put("totalOrdersInPeriod", "N/A (StatService 필요)");
//        stats.put("totalSalesInPeriod", "N/A (StatService 필요)");
//        return stats;
//    }
//
//    @Override
//    public Page<OrderDTO> getCustomerOrders(Integer customerId, Pageable pageable) {
//        log.info("특정 고객 주문 조회 (SalesLog 기반) - Customer ID: {}", customerId);
//        Page<SalesLog> salesLogsPage = salesLogRepository.findByCustomerId(customerId.longValue(), pageable);
//        List<OrderDTO> orderDTOs = salesLogsPage.getContent().stream()
//                .map(this::convertSalesLogToOrderDTO)
//                .collect(Collectors.toList());
//        return new PageImpl<>(orderDTOs, pageable, salesLogsPage.getTotalElements());
//    }
//
//    @Override
//    public Page<OrderDTO> getSellerOrders(Integer sellerId, Pageable pageable) {
//        log.info("특정 판매자 주문 조회 (SalesLog 기반) - Seller ID: {}", sellerId);
//        Page<SalesLog> salesLogsPage = salesLogRepository.findBySellerId(sellerId.longValue(), pageable);
//        List<OrderDTO> orderDTOs = salesLogsPage.getContent().stream()
//                .map(this::convertSalesLogToOrderDTO)
//                .collect(Collectors.toList());
//        return new PageImpl<>(orderDTOs, pageable, salesLogsPage.getTotalElements());
//    }
//
//    private OrderDTO convertSalesLogToOrderDTO(SalesLog sl) {
//        OrderDTO.Builder builder = OrderDTO.builder();
//        builder.id(sl.getOrderItemId() != null ? sl.getOrderItemId().longValue() : (sl.getId() != null ? sl.getId().longValue() : null) );
//        builder.orderDate(sl.getSoldAt() != null ? sl.getSoldAt().atStartOfDay() : null);
//        builder.totalAmount(sl.getTotalPrice() != null ? BigDecimal.valueOf(sl.getTotalPrice()) : BigDecimal.ZERO);
//        builder.status("COMPLETED"); // SalesLog는 판매 완료된 것으로 간주
//        builder.customerId(sl.getCustomerId() != null ? sl.getCustomerId().longValue() : null);
//        // SalesLog에 productId가 있다면 Product 정보 추가 가능
//        // builder.productId(sl.getProductId() != null ? sl.getProductId().longValue() : null);
//        // Product p = productRepository.findById(sl.getProductId().longValue()).orElse(null);
//        // if (p != null) builder.productName(p.getName());
//        return builder.build();
//    }
//}
