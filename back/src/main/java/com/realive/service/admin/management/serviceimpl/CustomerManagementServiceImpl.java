package com.realive.service.admin.management.serviceimpl;

import com.realive.dto.admin.management.CustomerDTO;
import com.realive.dto.admin.management.OrderDTO;
import com.realive.repository.CustomerRepository;
import com.realive.repository.OrderRepository;
import com.realive.service.admin.management.service.CustomerManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerManagementServiceImpl implements CustomerManagementService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Override
    public Page<CustomerDTO> getCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable) {
        return customerRepository.findByNameContainingOrEmailContaining(keyword, keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public CustomerDTO getCustomerById(Integer customerId) {
        return customerRepository.findById(customerId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new NoSuchElementException("고객 ID가 존재하지 않습니다: " + customerId));
    }

    @Override
    @Transactional
    public CustomerDTO updateCustomerStatus(Integer customerId, String status) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("고객 ID가 존재하지 않습니다: " + customerId));

        customer.setStatus(status);
        return convertToDTO(customerRepository.save(customer));
    }

    @Override
    public Page<OrderDTO> getCustomerOrders(Integer customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(this::convertToOrderDTO);
    }

    @Override
    public Map<String, Object> getCustomerStatistics(Integer customerId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("고객 ID가 존재하지 않습니다: " + customerId));

        // 주문 총액
        Integer totalSpent = orderRepository.calculateTotalSpentByCustomerId(customerId);

        // 주문 횟수
        Integer orderCount = orderRepository.countByCustomerId(customerId);

        // 최근 주문 날짜
        var latestOrder = orderRepository.findTopByCustomerIdOrderByOrderDateDesc(customerId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSpent", totalSpent != null ? totalSpent : 0);
        statistics.put("orderCount", orderCount);
        statistics.put("customerSince", customer.getRegisteredAt());
        statistics.put("latestOrderDate", latestOrder.map(order -> order.getOrderDate()).orElse(null));

        return statistics;
    }

    // 엔티티 -> DTO 변환 메소드
    private CustomerDTO convertToDTO(Customer customer) {
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
