package com.realive.service.admin.management.serviceimpl;


import com.realive.dto.admin.management.CustomerDTO;
import com.realive.dto.admin.management.OrderDTO;
import com.realive.dto.admin.management.ProductDTO;
import com.realive.dto.admin.management.SellerDTO;
import com.realive.repository.CustomerRepository;
import com.realive.repository.OrderRepository;
import com.realive.repository.ProductRepository;
import com.realive.repository.SellerRepository;
import com.realive.service.admin.management.dashboard.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;

    @Override
    public Map<String, Object> getDashboardSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minus(7, ChronoUnit.DAYS);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        // 총 회원 수
        Long totalCustomers = customerRepository.count();

        // 총 판매자 수
        Long totalSellers = sellerRepository.count();

        // 총 상품 수
        Long totalProducts = productRepository.count();

        // 오늘 주문 수
        Long todayOrders = orderRepository.countByOrderDateBetween(startOfDay, now);

        // 오늘 매출
        BigDecimal todaySales = orderRepository.sumTotalAmountByOrderDateBetween(startOfDay, now);

        // 이번 주 매출
        BigDecimal weeklySales = orderRepository.sumTotalAmountByOrderDateBetween(startOfWeek, now);

        // 이번 달 매출
        BigDecimal monthlySales = orderRepository.sumTotalAmountByOrderDateBetween(startOfMonth, now);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCustomers", totalCustomers);
        summary.put("totalSellers", totalSellers);
        summary.put("totalProducts", totalProducts);
        summary.put("todayOrders", todayOrders);
        summary.put("todaySales", todaySales != null ? todaySales : BigDecimal.ZERO);
        summary.put("weeklySales", weeklySales != null ? weeklySales : BigDecimal.ZERO);
        summary.put("monthlySales", monthlySales != null ? monthlySales : BigDecimal.ZERO);

        return summary;
    }

    @Override
    public Map<String, Object> getSalesStatistics(String period, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        Map<String, Object> statistics = new HashMap<>();

        // 전체 매출액
        BigDecimal totalSales = orderRepository.sumTotalAmountByOrderDateBetween(start, end);
        statistics.put("totalSales", totalSales != null ? totalSales : BigDecimal.ZERO);

        // 주문 건수
        Long orderCount = orderRepository.countByOrderDateBetween(start, end);
        statistics.put("orderCount", orderCount);

        // 평균 주문금액
        BigDecimal avgOrderAmount = orderCount > 0 && totalSales != null ?
                totalSales.divide(BigDecimal.valueOf(orderCount), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        statistics.put("avgOrderAmount", avgOrderAmount);

        // 기간에 따른 데이터 세분화
        Map<Object, BigDecimal> periodSales = new LinkedHashMap<>();

        if ("daily".equals(period)) {
            // 일별 매출
            periodSales = orderRepository.getDailySalesBetween(startDate, endDate)
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> BigDecimal.valueOf(entry.getValue())));
        } else if ("weekly".equals(period)) {
            // 주별 매출
            periodSales = orderRepository.getWeeklySalesBetween(startDate, endDate);
        } else if ("monthly".equals(period)) {
            // 월별 매출
            periodSales = orderRepository.getMonthlySalesBetween(startDate, endDate);
        }

        statistics.put("periodSales", periodSales);

        return statistics;
    }

    @Override
    public List<CustomerDTO> getNewCustomers(int limit) {
        return customerRepository.findTop10ByOrderByRegisteredAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::convertToCustomerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getTopSellingProducts(int limit) {
        return productRepository.findTopSellingProducts(PageRequest.of(0, limit))
                .stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SellerDTO> getTopSellers(int limit) {
        return sellerRepository.findTopSellersByTotalSales(PageRequest.of(0, limit))
                .stream()
                .map(this::convertToSellerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getRecentOrders(int limit) {
        return orderRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "orderDate")))
                .stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SellerDTO> getPendingSellers() {
        return sellerRepository.findByStatus("PENDING")
                .stream()
                .map(this::convertToSellerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getPendingProducts() {
        return productRepository.findByStatus("PENDING")
                .stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }

    // 엔티티 -> DTO 변환 메소드
    private CustomerDTO convertToCustomerDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setStatus(customer.getStatus());
        dto.setRegisteredAt(customer.getRegisteredAt());
        dto.setOrderCount(customer.getOrderCount());
        dto.setTotalSpent(customer.getTotalSpent());
        return dto;
    }

    private ProductDTO convertToProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSellerId(product.getSeller().getId());
        dto.setSellerName(product.getSeller().getName());
        dto.setStatus(product.getStatus());
        dto.setPrice(product.getPrice());
        dto.setInventory(product.getInventory());
        dto.setRegisteredAt(product.getRegisteredAt());
        return dto;
    }

    private SellerDTO convertToSellerDTO(Seller seller) {
        SellerDTO dto = new SellerDTO();
        dto.setId(seller.getId());
        dto.setName(seller.getName());
        dto.setStatus(seller.getStatus());
        dto.setRegisteredAt(seller.getRegisteredAt());
        dto.setProductCount(seller.getProductCount());
        dto.setTotalSales(seller.getTotalSales());
        dto.setCommission(seller.getCommission());
        return dto;
    }

    private OrderDTO convertToOrderDTO(Order order) {
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