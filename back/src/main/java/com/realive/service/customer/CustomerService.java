package com.realive.service.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.domain.customer.Customer;
import com.realive.repository.customer.CustomerRepository;

import lombok.RequiredArgsConstructor;

// 고객 서비스

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    // 고객 ID로 활성화된 회원 정보 조회
    public Customer getActiveCustomerById(Long id) {
        return customerRepository.findActiveUserById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않거나 비활성 상태입니다."));
    }

    // 이메일로 고객 정보 조회 (소셜 로그인 제외)
    public Long findIdByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 없음"))
                .getId();
    }

}

