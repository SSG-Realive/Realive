package com.realive.serviceimpl.admin.product;

import com.realive.domain.admin.Admin;
import com.realive.domain.auction.AdminProduct;
import com.realive.domain.product.Product;
import com.realive.dto.auction.AdminPurchaseRequestDTO;
import com.realive.dto.auction.AdminProductDTO;
import com.realive.repository.admin.AdminRepository;
import com.realive.repository.auction.AdminProductRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.service.admin.product.AdminProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductServiceImpl implements AdminProductService {

    private final AdminProductRepository adminProductRepository;
    private final ProductRepository productRepository;
    private final AdminRepository adminRepository;

    @Override
    @Transactional
    public AdminProductDTO purchaseProduct(AdminPurchaseRequestDTO requestDTO, Integer adminId) {
        // 1. 관리자 존재 확인
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        // 2. 원본 상품 존재 확인
        Product product = productRepository.findById(requestDTO.getProductId().longValue())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 3. 이미 매입된 상품인지 확인
        if (adminProductRepository.findByProductId(requestDTO.getProductId().intValue()).isPresent()) {
            throw new IllegalStateException("이미 매입된 상품입니다.");
        }

        // 4. AdminProduct 생성 및 저장
        AdminProduct adminProduct = requestDTO.toEntity();
        AdminProduct savedAdminProduct = adminProductRepository.save(adminProduct);

        log.info("관리자 상품 매입 완료 - 관리자 ID: {}, 상품 ID: {}, 매입 가격: {}", 
                adminId, requestDTO.getProductId(), requestDTO.getPurchasePrice());

        return AdminProductDTO.fromEntity(savedAdminProduct, product);
    }

    @Override
    public AdminProductDTO getAdminProduct(Integer productId) {
        AdminProduct adminProduct = adminProductRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 상품입니다."));

        Product product = productRepository.findById(adminProduct.getProductId().longValue())
                .orElse(null);

        return AdminProductDTO.fromEntity(adminProduct, product);
    }
} 