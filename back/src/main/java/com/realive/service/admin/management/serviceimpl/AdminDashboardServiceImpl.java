package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.common.enums.ProductStatus;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.product.Product;
import com.realive.domain.seller.Seller;
import com.realive.dto.admin.management.CustomerDTO; // 첨부된 CustomerDTO 사용
import com.realive.dto.admin.management.OrderDTO;
import com.realive.dto.admin.management.ProductDTO;
import com.realive.dto.admin.management.SellerDTO;
import com.realive.dto.logs.salessum.DailySalesSummaryDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.seller.SellerRepository; // 첨부된 SellerRepository 사용 (제네릭 및 반환 타입 수정 필요)
import com.realive.service.admin.logs.StatService;
import com.realive.service.admin.management.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
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
        return
