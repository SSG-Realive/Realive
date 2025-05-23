package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.customer.Customer;
import com.realive.domain.logs.SalesLog; // SalesLog 엔티티 경로
import com.realive.dto.admin.management.CustomerDTO;
import com.realive.dto.admin.management.OrderDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.logs.SalesLogRepository; // SalesLogRepository 주입
// import com.realive.service.admin.logs.StatService; // StatService 주입 (선택적)
import com.realive.service.admin.management.service.CustomerManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl; // 추가
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List; // 추가
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors; // 추가

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerManagementServiceImpl implements CustomerManagementService {

    private final CustomerRepository customerRepository;
    private final SalesLogRepository salesLogRepository; // 고객 주문/매출 통계용
    // private final StatService statService; // 또는 StatService를 통해 조회

    @Override
    public Page<CustomerDTO> getCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::convertToCustomerDTO);
    }

    @Override
    public Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) return getCustomers(pageable);
        return customerRepository.findByNameOrEmailContainingIgnoreCase(keyword, pageable)
                .map(this::convertToCustomerDTO);
    }

    @Override
    public CustomerDTO getCustomerById(Integer customerId) {
        Customer customer = customerRepository.findById(customerId.longValue())
                .orElseThrow(() -> new NoSuchElementException("고객 없음 ID: " + customerId));
        return convertToCustomerDTO(customer);
    }

    @Override
    @Transactional
    public CustomerDTO updateCustomerStatus(Integer customerId, String status) {
        Customer customer = customerRepository.findById(customerId.longValue())
                .orElseThrow(() -> new NoSuchElementException("고객 없음 ID: " + customerId));
        if ("ACTIVE".equalsIgnoreCase(status)) customer.setIsActive(true);
        else if ("INACTIVE".equalsIgnoreCase(status)) customer.setIsActive(false);
        else throw new IllegalArgumentException("유효하지 않은 상태값: " + status);
        return convertToCustomerDTO(customerRepository.save(customer));
    }

    @Override
    public Page<OrderDTO> getCustomerOrders(Integer customerId, Pageable pageable) {
        log.info("고객 주문 이력 조회 (SalesLog 기반) - Customer ID: {}", customerId);
        // SalesLogRepository에 findByCustomerId(Long customerId, Pageable pageable) 메소드 필요
        Page<SalesLog> salesLogsPage = salesLogRepository.findByCustomerId(customerId.longValue(), pageable);
        List<OrderDTO> orderDTOs = salesLogsPage.getContent().stream()
                .map(this::convertSalesLogToOrderDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(orderDTOs, pageable, salesLogsPage.getTotalElements());
    }

    @Override
    public Map<String, Object> getCustomerStatistics(Integer customerId) {
        log.info("고객 통계 조회 (SalesLog 기반) - Customer ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId.longValue())
                .orElseThrow(() -> new NoSuchElementException("고객 없음 ID: " + customerId));
        Map<String, Object> stats = new HashMap<>();
        stats.put("customerId", customer.getId());
        stats.put("customerName", customer.getName());
        stats.put("registeredAt", customer.getCreated());

        // SalesLogRepository에 고객별 집계 메소드 필요
        // 예: countDistinctOrderByCustomerId, sumTotalPriceByCustomerId
        Long totalOrders = salesLogRepository.countDistinctOrdersByCustomerId(customerId.longValue());
        BigDecimal totalSpent = salesLogRepository.sumTotalPriceByCustomerId(customerId.longValue());

        stats.put("totalOrders", totalOrders != null ? totalOrders : 0L);
        stats.put("totalSpent", totalSpent != null ? totalSpent : BigDecimal.ZERO);
        return stats;
    }

    private CustomerDTO convertToCustomerDTO(Customer e) {
        return CustomerDTO.builder().id(e.getId()).name(e.getName()).email(e.getEmail()).status(e.getIsActive() ? "ACTIVE" : "INACTIVE").registeredAt(e.getCreated()).build();
    }

    private OrderDTO convertSalesLogToOrderDTO(SalesLog sl) {
        // SalesLog 정보를 OrderDTO로 변환 (OrderDTO 구조에 맞게 필드 매핑)
        // SalesLog에 주문 관련 주요 정보(주문 ID, 상품 정보 요약 등)가 있다면 활용
        OrderDTO.Builder builder = OrderDTO.builder();
        // SalesLog에 orderId 또는 orderItemId가 있다면 DTO의 id로 사용
        builder.id(sl.getOrderItemId() != null ? sl.getOrderItemId().longValue() : (sl.getId() != null ? sl.getId().longValue() : null) );
        builder.orderDate(sl.getSoldAt() != null ? sl.getSoldAt().atStartOfDay() : null); // soldAt은 LocalDate 가정
        builder.totalAmount(sl.getTotalPrice() != null ? BigDecimal.valueOf(sl.getTotalPrice()) : BigDecimal.ZERO);
        builder.status("COMPLETED"); // SalesLog는 판매 완료된 것으로 간주
        builder.customerId(sl.getCustomerId() != null ? sl.getCustomerId().longValue() : null);
        // 추가적으로 productId, productName 등을 SalesLog 또는 연관 Product 정보에서 가져와 설정
        // builder.productName(sl.getProduct() != null ? sl.getProduct().getName() : "N/A");
        return builder.build();
    }
}
