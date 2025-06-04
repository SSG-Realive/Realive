package com.realive.service.admin.product;


import com.realive.dto.admin.ProductDetailDTO;
import com.realive.dto.auction.AdminPurchaseRequestDTO;
import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.product.ProductListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductManagementService{



    // 전체/판매자별 상품 목록 조회 (필터: 검색어, 활성화 상태)
    Page<ProductListDTO> getProducts(Long sellerId, String search, Boolean active, Pageable pageable);

    // 상품 상세 조회
    ProductDetailDTO getProductDetails(Long productId);

    // 상품 비활성화 (삭제 대체)
    void deactivateProduct(Long productId);


}