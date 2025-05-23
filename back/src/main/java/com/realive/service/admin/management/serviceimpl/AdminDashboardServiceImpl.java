//package com.realive.service.admin.management.serviceimpl;
//
//import com.realive.domain.common.enums.ProductStatus;
//import com.realive.domain.customer.Customer;
//import com.realive.domain.order.Order;
//import com.realive.domain.product.Product;
//import com.realive.domain.seller.Seller;
//import com.realive.dto.admin.management.CustomerDTO;
//import com.realive.dto.admin.management.OrderDTO;
//import com.realive.dto.admin.management.ProductDTO;
//import com.realive.dto.admin.management.SellerDTO;
//import com.realive.repository.customer.CustomerRepository;
//import com.realive.repository.order.OrderRepository;
//import com.realive.repository.product.ProductRepository;
//import com.realive.repository.seller.SellerRepository;
//import com.realive.service.admin.logs.StatService; // StatService 주입
//import com.realive.service.admin.management.service.AdminDashboardService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal; // 추가
//import java.time.LocalDate;
//import java.util.ArrayList; // 추가
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class AdminDashboardServiceImpl implements AdminDashboardService {
//
//    private final CustomerRepository customerRepository;
//    private final OrderRepository orderRepository; // Order 엔티티 정보가 제한적이므로 사용 최소화
//    private final ProductRepository productRepository;
//    private final SellerRepository sellerRepository;
//    private final StatService statService; // 통계 처리를 위해 StatService 적극 활용
//
//    @Override
//    public Map<String, Object> getDashboardSummary() {
//        log.info("대시보드 요약 정보 조회 (StatService 활용)");
//        Map<String, Object> summary = new HashMap<>();
//
//        summary.put("totalCustomers", customerRepository.count());
//        summary.put("totalSellers", sellerRepository.count());
//        summary.put("totalProducts", productRepository.count());
//        // 총 주문 건수는 SalesLog 기반으로 StatService에서 가져오는 것이 정확
//        // summary.put("totalOrders", statService.getTotalOrderCountFromSalesLog()); // StatService에 이런 메소드 필요
//
//        // StatService의 getDashboardStats 활용
//        Map<String, Object> dashboardStats = statService.getDashboardStats(LocalDate.now());
//        if (dashboardStats != null) {
//            summary.putAll(dashboardStats);
//        } else {
//            log.warn("StatService.getDashboardStats() 반환값이 null입니다.");
//            // 기본값 또는 오류 메시지 설정
//            summary.put("todayOrdersCount", 0L);
//            summary.put("todayTotalSales", BigDecimal.ZERO);
//        }
//        // 주간/월간 매출은 getSalesStatistics를 특정 기간으로 호출하거나, StatService에 별도 메소드 필요
//        summary.putIfAbsent("currentWeekSales", "N/A (기간별 통계 또는 StatService 확장 필요)");
//        summary.putIfAbsent("currentMonthSales", "N/A (기간별 통계 또는 StatService 확장 필요)");
//
//        return summary;
//    }
//
//    @Override
//    public Map<String, Object> getSalesStatistics(String period, LocalDate startDate, LocalDate endDate) {
//        log.info("매출 통계 조회 (StatService 활용) - 기간: {} ~ {}, 주기: {}", startDate, endDate, period);
//        Map<String, Object> statistics = new HashMap<>();
//        statistics.put("period", period);
//        statistics.put("startDate", startDate.toString());
//        statistics.put("endDate", endDate.toString());
//
//        // StatService의 메소드를 활용하여 SalesLog 기반 통계 조회
//        // 이 부분은 StatService 인터페이스에 정의된 메소드와 그 반환 타입에 맞춰야 함
//        if ("daily".equalsIgnoreCase(period)) {
//            List<com.realive.dto.logs.salessum.DailySalesSummaryDTO> dailySummaries = new ArrayList<>();
//            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
//                dailySummaries.add(statService.getDailySalesSummary(date));
//            }
//            statistics.put("periodSalesDetails", dailySummaries);
//            statistics.put("totalSales", dailySummaries.stream().map(s -> s.getTotalSalesAmount() != null ? BigDecimal.valueOf(s.getTotalSalesAmount()) : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add));
//            statistics.put("totalOrders", dailySummaries.stream().mapToLong(s -> s.getTotalSalesCount() != null ? s.getTotalSalesCount() : 0L).sum());
//        } else if ("monthly".equalsIgnoreCase(period)) {
//            List<com.realive.dto.logs.salessum.MonthlySalesSummaryDTO> monthlySummaries = new ArrayList<>();
//            java.time.YearMonth startYM = java.time.YearMonth.from(startDate);
//            java.time.YearMonth endYM = java.time.YearMonth.from(endDate);
//            for (java.time.YearMonth ym = startYM; !ym.isAfter(endYM); ym = ym.plusMonths(1)) {
//                monthlySummaries.add(statService.getMonthlySalesSummary(ym));
//            }
//            statistics.put("periodSalesDetails", monthlySummaries);
//            statistics.put("totalSales", monthlySummaries.stream().map(s -> s.getTotalSalesAmount() != null ? BigDecimal.valueOf(s.getTotalSalesAmount()) : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add));
//            statistics.put("totalOrders", monthlySummaries.stream().mapToLong(s -> s.getTotalSalesCount() != null ? s.getTotalSalesCount() : 0L).sum());
//        } else {
//            statistics.put("message", period + " 주기의 통계는 현재 StatService에서 직접 지원하지 않거나 별도 집계 필요.");
//            statistics.put("periodSalesDetails", Collections.emptyList());
//            statistics.put("totalSales", BigDecimal.ZERO);
//            statistics.put("totalOrders", 0L);
//        }
//        // 평균 주문 금액 계산 (totalSales와 totalOrders가 BigDecimal, Long으로 정확히 계산된 후)
//        BigDecimal totalSalesBd = (BigDecimal) statistics.getOrDefault("totalSales", BigDecimal.ZERO);
//        Long totalOrdersLng = (Long) statistics.getOrDefault("totalOrders", 0L);
//        statistics.put("averageOrderAmount", totalOrdersLng > 0 ? totalSalesBd.divide(BigDecimal.valueOf(totalOrdersLng), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
//
//        return statistics;
//    }
//
//    @Override
//    public List<CustomerDTO> getNewCustomers(int limit) {
//        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "created"));
//        return customerRepository.findAll(pageable).stream()
//                .map(this::convertToCustomerDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ProductDTO> getTopSellingProducts(int limit) {
//        // SalesLog 기반으로 판매량/판매액 기준 인기 상품을 StatService에서 조회하는 것이 더 정확
//        // 예: List<ProductSalesSummaryDTO> topProductsStats = statService.getTopSellingProductsBySales(limit);
//        // 이 결과를 ProductDTO로 변환해야 함.
//        // 현재는 OrderRepository의 제한된 기능(주문 횟수 기준) 사용.
//        Pageable pageable = PageRequest.of(0, limit);
//        List<Product> topProducts = orderRepository.findTopOrderedProducts(pageable);
//        return topProducts.stream()
//                .map(this::convertToProductDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<SellerDTO> getTopSellers(int limit) {
//        // SalesLog 기반으로 판매자별 매출/판매량 집계 후 상위 N개 조회 기능이 StatService에 필요
//        // 예: List<SellerSalesSummaryDTO> topSellersStats = statService.getTopPerformingSellersBySales(limit);
//        // 이 결과를 SellerDTO로 변환해야 함.
//        log.warn("getTopSellers: StatService에 판매자별 실적 조회 기능 구현 필요.");
//        return Collections.emptyList();
//    }
//
//    @Override
//    public List<OrderDTO> getRecentOrders(int limit) {
//        // SalesLog 기반으로 실제 판매/결제 완료된 최근 "판매 기록"을 가져오는 것이 더 의미 있음
//        // 예: Page<SalesLog> recentSales = salesLogRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "soldAt")));
//        // 이 SalesLog를 OrderDTO와 유사한 형태로 변환 (날짜, 금액, 고객, 상품 정보 등 포함)
//        log.warn("getRecentOrders: SalesLog 기반으로 최근 '판매 기록' 조회 및 변환 권장. 현재는 Order ID 기준.");
//        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
//        return orderRepository.findAll(pageable).stream()
//                .map(this::convertToOrderDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<SellerDTO> getPendingSellers() {
//        return sellerRepository.findByIsApprovedFalse().stream()
//                .map(this::convertToSellerDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ProductDTO> getPendingProducts() {
//        // ProductRepository.findByStatus와 ProductStatus Enum의 승인대기 값 (예: PENDING_APPROVAL) 필요
//        return productRepository.findByStatus(ProductStatus.PENDING_APPROVAL).stream()
//                .map(this::convertToProductDTO)
//                .collect(Collectors.toList());
//    }
//
//    // --- DTO 변환 메소드 ---
//    private CustomerDTO convertToCustomerDTO(Customer e) {
//        return CustomerDTO.builder().id(e.getId()).name(e.getName()).email(e.getEmail()).status(e.getIsActive() ? "ACTIVE" : "INACTIVE").registeredAt(e.getCreated()).build();
//    }
//    private ProductDTO convertToProductDTO(Product e) {
//        return ProductDTO.builder().id(e.getId()).name(e.getName()).price(BigDecimal.valueOf(e.getPrice())).inventory(e.getStock()).status(e.getStatus() != null ? e.getStatus().name() : null).sellerId(e.getSeller() != null ? e.getSeller().getId() : null).sellerName(e.getSeller() != null ? e.getSeller().getName() : null).registeredAt(e.getCreatedAt()).build();
//    }
//    private SellerDTO convertToSellerDTO(Seller e) {
//        return SellerDTO.builder().id(e.getId()).name(e.getName()).email(e.getEmail()).status(e.isApproved() ? "APPROVED" : "PENDING").registeredAt(e.getCreatedAt()).build();
//    }
//    private OrderDTO convertToOrderDTO(Order e) { // Order 엔티티 정보가 매우 제한적
//        OrderDTO.Builder builder = OrderDTO.builder().id(e.getId());
//        // if (e.getProduct() != null) { builder.productId(e.getProduct().getId()); builder.productName(e.getProduct().getName()); }
//        // Order 엔티티에 customer, orderDate, totalAmount, status 등이 없으므로 이 정보는 DTO에 담을 수 없음
//        return builder.build();
//    }
//}
