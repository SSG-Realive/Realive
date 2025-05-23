package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.common.enums.ProductStatus;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.product.Product;
import com.realive.domain.seller.Seller;
import com.realive.dto.admin.management.CustomerDTO;
import com.realive.dto.admin.management.OrderDTO;
import com.realive.dto.admin.management.ProductDTO;
import com.realive.dto.admin.management.SellerDTO;
import com.realive.dto.logs.salessum.DailySalesSummaryDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.seller.SellerRepository; // 첨부된 SellerRepository 사용
import com.realive.service.admin.logs.StatService;
import com.realive.service.admin.management.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page; // SellerRepository.findByIsApproved 반환 타입
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
// import java.util.Collection; // 이전 버전 SellerRepository.findByIsApprovedFalse() 반환 타입
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final StatService statService;

    @Override
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCustomers", customerRepository.count());
        summary.put("totalSellers", sellerRepository.count());
        summary.put("totalProducts", productRepository.count());
        summary.put("totalOrderRecords", orderRepository.count());

        Map<String, Object> dashboardStats = statService.getDashboardStats(LocalDate.now());
        if (dashboardStats != null && dashboardStats.get("dailySalesSummary") instanceof DailySalesSummaryDTO) {
            DailySalesSummaryDTO dailySummary = (DailySalesSummaryDTO) dashboardStats.get("dailySalesSummary");
            summary.put("todaySalesCount", dailySummary.getTotalSalesCount() != null ? dailySummary.getTotalSalesCount().longValue() : 0L);
            summary.put("todayTotalSalesAmount", dailySummary.getTotalSalesAmount() != null ? BigDecimal.valueOf(dailySummary.getTotalSalesAmount()) : BigDecimal.ZERO);
        } else {
            summary.put("todaySalesCount", 0L);
            summary.put("todayTotalSalesAmount", BigDecimal.ZERO);
        }
        summary.putIfAbsent("currentWeekSales", "N/A");
        summary.putIfAbsent("currentMonthSales", "N/A");
        return summary;
    }

    @Override
    public Map<String, Object> getSalesStatistics(String period, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("period", period);
        statistics.put("startDate", startDate.toString());
        statistics.put("endDate", endDate.toString());

        BigDecimal totalSalesForPeriod = BigDecimal.ZERO;
        long totalOrdersForPeriod = 0L;
        List<?> periodDetailsData = new ArrayList<>();

        try {
            if ("daily".equalsIgnoreCase(period)) {
                List<DailySalesSummaryDTO> dailySummaries = statService.getDailySummariesInMonth(YearMonth.from(startDate));
                List<DailySalesSummaryDTO> filteredDailySummaries = dailySummaries.stream()
                        .filter(ds -> ds.getDate() != null && !ds.getDate().isBefore(startDate) && !ds.getDate().isAfter(endDate))
                        .collect(Collectors.toList());
                periodDetailsData = filteredDailySummaries;
                totalSalesForPeriod = filteredDailySummaries.stream().map(s -> s.getTotalSalesAmount() != null ? BigDecimal.valueOf(s.getTotalSalesAmount()) : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                totalOrdersForPeriod = filteredDailySummaries.stream().mapToLong(s -> s.getTotalSalesCount() != null ? s.getTotalSalesCount().longValue() : 0L).sum();
            } else if ("monthly".equalsIgnoreCase(period)) {
                List<MonthlySalesSummaryDTO> monthlySummaries = new ArrayList<>();
                YearMonth startYM = YearMonth.from(startDate);
                YearMonth endYM = YearMonth.from(endDate);
                for (YearMonth ym = startYM; !ym.isAfter(endYM); ym = ym.plusMonths(1)) {
                    monthlySummaries.add(statService.getMonthlySalesSummary(ym));
                }
                periodDetailsData = monthlySummaries;
                totalSalesForPeriod = monthlySummaries.stream().map(s -> s.getTotalSalesAmount() != null ? BigDecimal.valueOf(s.getTotalSalesAmount()) : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                totalOrdersForPeriod = monthlySummaries.stream().mapToLong(s -> s.getTotalSalesCount() != null ? s.getTotalSalesCount().longValue() : 0L).sum();
            }
        } catch (Exception e) {
            log.error("StatService 호출 중 오류 발생 (getSalesStatistics): {}", e.getMessage(), e);
            statistics.put("error", "통계 조회 중 오류 발생");
        }

        statistics.put("periodSalesDetails", periodDetailsData);
        statistics.put("totalSales", totalSalesForPeriod);
        statistics.put("totalOrders", totalOrdersForPeriod);
        statistics.put("averageOrderAmount", totalOrdersForPeriod > 0 ? totalSalesForPeriod.divide(BigDecimal.valueOf(totalOrdersForPeriod), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
        return statistics;
    }

    @Override
    public List<CustomerDTO> getNewCustomers(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return customerRepository.findAll(pageable).stream()
                .map(this::convertToCustomerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getTopSellingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> topProducts = orderRepository.findTopOrderedProducts(pageable);
        return topProducts.stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SellerDTO> getTopSellers(int limit) {
        return Collections.emptyList();
    }

    @Override
    public List<OrderDTO> getRecentOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return orderRepository.findAll(pageable).stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked") // SellerRepository의 findByIsApproved가 raw Page를 반환하므로 경고를 무시합니다. Repository 수정 필요.
    public List<SellerDTO> getPendingSellers() {
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Seller> pendingSellersPage = sellerRepository.findByIsApproved(false, pageable);
        if (pendingSellersPage != null) {
            return pendingSellersPage.getContent().stream()
                    .map(this::convertToSellerDTO)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<ProductDTO> getPendingProducts() {
        // ProductStatus Enum에 PENDING_APPROVAL과 같은 상태가 정의되어 있어야 함.
        // Pageable pageable = PageRequest.of(0, 100);
        // return productRepository.findByStatus(ProductStatus.PENDING_APPROVAL, pageable).getContent().stream()
        // .map(this::convertToProductDTO)
        // .collect(Collectors.toList());
        return Collections.emptyList();
    }

    private CustomerDTO convertToCustomerDTO(Customer e) {
        if (e == null) return null;
        return CustomerDTO.builder()
                .id(e.getId() != null ? e.getId().intValue() : null)
                .name(e.getName())
                .email(e.getEmail())
                .status(e.isActive() ? "ACTIVE" : "INACTIVE")
                .registeredAt(e.getCreatedAt())
                .orderCount(0)
                .totalSpent(0)
                .build();
    }
    private ProductDTO convertToProductDTO(Product e) {
        if (e == null) return null;
        return ProductDTO.builder()
                .id(e.getId() != null ? e.getId().intValue() : null)
                .name(e.getName())
                .price(BigDecimal.valueOf(e.getPrice()))
                .inventory(e.getStock())
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .sellerId(e.getSeller() != null && e.getSeller().getId() != null ? e.getSeller().getId().intValue() : null)
                .sellerName(e.getSeller() != null ? e.getSeller().getName() : null)
                .registeredAt(e.getCreatedAt())
                .build();
    }
    private SellerDTO convertToSellerDTO(Seller e) {
        if (e == null) return null;
        return SellerDTO.builder()
                .id(e.getId() != null ? e.getId().intValue() : null)
                .name(e.getName())
                .status(e.isApproved() ? "APPROVED" : "PENDING")
                .registeredAt(e.getCreatedAt())
                .productCount(0)
                .totalSales(0)
                .commission(e.getCommissionRate() != null ? e.getCommissionRate() : BigDecimal.ZERO)
                .build();
    }
    private OrderDTO convertToOrderDTO(Order e) {
        if (e == null) return null;
        OrderDTO.Builder builder = OrderDTO.builder()
                .id(e.getId() != null ? e.getId().intValue() : null);
        return builder.build();
    }
}
