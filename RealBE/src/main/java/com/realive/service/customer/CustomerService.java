package com.realive.service.customer;

import com.realive.domain.customer.Customer;
import com.realive.dto.auction.CustomerProfileDTO;
import com.realive.exception.NotFoundException;
import com.realive.repository.customer.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 일반 이메일 조회 (소셜 제외)
    public Customer getByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 이메일의 회원을 찾을 수 없습니다."));
    }

    // 소셜 포함 이메일 조회
    public Customer getByEmailIncludingSocial(String email) {
        return customerRepository.findByEmailIncludingSocial(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 이메일의 회원을 찾을 수 없습니다."));
    }
    // 경매 낙찰 시 고객 프로필 정보 조회
     public CustomerProfileDTO getProfile(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("회원이 존재하지 않습니다."));

        return CustomerProfileDTO.builder()
                .receiverName(customer.getName())
                .phone(customer.getPhone())
                .deliveryAddress(customer.getAddress())
                .build();
    }
}