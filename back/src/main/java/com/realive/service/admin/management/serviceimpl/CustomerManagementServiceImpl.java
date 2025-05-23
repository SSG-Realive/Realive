package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.customer.Customer;
import com.realive.domain.logs.SalesLog;
import com.realive.dto.admin.management.CustomerDTO;
import com.realive.dto.admin.management.OrderDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.logs.SalesLogRepository;
import com.realive.service.admin.management.service.CustomerManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class CustomerManagementServiceImpl implements CustomerManagementService {

    private final CustomerRepository customerRepository;
    private final SalesLogRepository salesLogRepository;

    @Override
    public Page<CustomerDTO> getCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::convertToCustomerDTO);
    }

    @Override
    public Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getCustomers(pageable);
        }
        // CustomerRepository에 findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase 메소드가 정의되어 있다고 가정
        return customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::convertToCustomerDTO);
    }

    @Override
    public CustomerDTO getCustomerById(Integer customerId) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId.longValue());
        Customer customer = customerOptional.orElseThrow(() -> new NoSuchElementException("고객 없음 ID: " + customerId));
        return convertToCustomerDTO(customer);
    }

    @Override
    @Transactional
    public CustomerDTO updateCustomerStatus(Integer customerId, String status) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId.longValue());
        Customer customer = customerOptional.orElseThrow(() -> new NoSuchElementException("고객 없음 ID: " + customerId));

        if ("ACTIVE".equalsIgnoreCase(status)) {
            customer.setActive(true);
        } else if ("INACTIVE".equalsIgnoreCase(status)) {
            customer.setActive(false);
        } else {
            throw new IllegalArgumentException("유효하지 않은 상태값: " + status);
        }
        return convertToCustomerDTO(customerRepository.save(customer));
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
    public Map<String, Object> getCustomerStatistics(Integer customerId) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId.longValue());
        Customer customer = customerOptional.orElseThrow(() -> new NoSuchElementException("고객 없음 ID: " + customerId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("customerId", customer.getId() != null ? customer.getId().intValue() : null);
        stats.put("customerName", customer.getName());
        stats.put("registeredAt", customer.getCreatedAt());

        Integer totalOrders = salesLogRepository.countDistinctOrdersByCustomerId(customerId);
        Integer totalSpent = salesLogRepository.sumTotalPriceByCustomerId(customerId);

        stats.put("totalOrders", totalOrders != null ? totalOrders : 0);
        stats.put("totalSpent", totalSpent != null ? totalSpent : 0);
        return stats;
    }

    private CustomerDTO convertToCustomerDTO(Customer e) {
        if (e == null) return null;
        return CustomerDTO.builder()
                .id(e.getId() != null ? e.getId().intValue() : null)
                .name(e.getName())
                .email(e.getEmail())
                .status(e.isActive() ? "ACTIVE" : "INACTIVE")
                .registeredAt(e.getCreatedAt())
                .orderCount(e.getOrderCount())
                .totalSpent(e.getTotalSpent())
                .build();
    }

    private OrderDTO convertSalesLogToOrderDTO(SalesLog sl) {
        if (sl == null) return null;
        return OrderDTO.builder()
                .id(sl.getOrderItemId() != null ? sl.getOrderItemId() : sl.getId())
                .customerId(sl.getCustomerId())
                .status("COMPLETED")
                .orderDate(sl.getSoldAt() != null ? sl.getSoldAt().atStartOfDay() : null)
                .totalAmount(sl.getTotalPrice())
                .build();
    }
}
//수정 다수 필요