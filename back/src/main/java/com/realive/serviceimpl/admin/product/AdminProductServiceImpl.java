package com.realive.serviceimpl.admin.product;

import com.realive.domain.admin.Admin;
import com.realive.domain.auction.AdminProduct;
import com.realive.domain.product.Product;
import com.realive.domain.seller.Seller;
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
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

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
        log.info("관리자 상품 매입 처리 시작: adminId={}, productId={}, price={}", 
            adminId, requestDTO.getProductId(), requestDTO.getPurchasePrice());

        // 1. 관리자 존재 확인
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 관리자입니다."));

        // 2. 상품 존재 여부 확인
        Product product = productRepository.findById(requestDTO.getProductId().longValue())
            .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        // 3. 상품 상태 확인
        if (!product.isActive()) {
            throw new IllegalStateException("비활성화된 상품은 매입할 수 없습니다.");
        }

        // 4. 상품 수량 확인
        if (product.getStock() <= 0) {
            throw new IllegalStateException("재고가 없는 상품은 매입할 수 없습니다.");
        }

        // 5. 판매자 확인
        Seller seller = product.getSeller();
        if (seller == null) {
            throw new IllegalStateException("판매자 정보가 없는 상품입니다.");
        }

        // 6. 이미 매입된 상품인지 확인
        if (adminProductRepository.findByProductId(requestDTO.getProductId()).isPresent()) {
            throw new IllegalStateException("이미 매입된 상품입니다.");
        }

        // 7. 판매자의 상품 수량 감소
        product.setStock(product.getStock() - 1);
        productRepository.save(product);

        // 8. AdminProduct 생성
        AdminProduct adminProduct = AdminProduct.builder()
            .productId(requestDTO.getProductId())
            .purchasePrice(requestDTO.getPurchasePrice())
            .purchasedFromSellerId(seller.getId().intValue())
            .purchasedAt(LocalDateTime.now())
            .isAuctioned(false)
            .build();

        // 9. AdminProduct 저장
        AdminProduct savedAdminProduct = adminProductRepository.save(adminProduct);
        log.info("관리자 상품 매입 완료: adminProductId={}", savedAdminProduct.getId());

        return AdminProductDTO.fromEntity(savedAdminProduct, product);
    }

    @Override
    public AdminProductDTO getAdminProduct(Integer productId) {
        AdminProduct adminProduct = adminProductRepository.findByProductId(productId)
            .orElseThrow(() -> new NoSuchElementException("관리자 상품을 찾을 수 없습니다."));
        
        Product product = productRepository.findById(productId.longValue())
            .orElse(null);

        return AdminProductDTO.fromEntity(adminProduct, product);
    }
} 