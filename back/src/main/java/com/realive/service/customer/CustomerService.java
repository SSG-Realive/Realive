package com.realive.service.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.domain.customer.Customer;
import com.realive.repository.customer.CustomerRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

// [Customer] 회원 정보 조회 Service

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    // 고객 ID로 활성화된 회원 정보 조회
    public Customer getActiveCustomerById(Long id) {
        return customerRepository.findActiveUserById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않거나 비활성 상태입니다."));
    }

    // 이메일로 고객 정보 조회 (임시회원 제외)
    public Long findIdByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"))
                .getId();
    }

}

