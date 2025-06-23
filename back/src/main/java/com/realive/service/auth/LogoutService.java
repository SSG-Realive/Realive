package com.realive.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.repository.admin.AdminRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.seller.SellerRepository;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public void customerLogout(Long customerId) {
        // ID로 고객을 찾아 refreshToken을 null로 업데이트
        customerRepository.findById(customerId).ifPresent(customer -> customer.setRefreshToken(null));
    }

    @Transactional
    public void sellerLogout(Long sellerId) {
        // ID로 판매자를 찾아 refreshToken을 null로 업데이트
        sellerRepository.findById(sellerId).ifPresent(seller -> seller.setRefreshToken(null));
    }

    @Transactional
    public void adminLogout(Integer adminId) {
        // ID로 관리자를 찾아 refreshToken을 null로 업데이트
        adminRepository.findById(adminId).ifPresent(admin -> admin.setRefreshToken(null));
    }
}