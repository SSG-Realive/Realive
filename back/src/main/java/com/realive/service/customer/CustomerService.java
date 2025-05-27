package com.realive.service.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.domain.customer.Customer;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.customerlogin.CustomerLoginRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerLoginRepository customerLoginRepository;

    public Customer getActiveCustomerById(Long id) {
        return customerRepository.findActiveUserById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않거나 비활성 상태입니다."));
    }

    public Long findIdByEmail(String email) {
        return customerLoginRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 없음"))
                .getId();
    }

}

