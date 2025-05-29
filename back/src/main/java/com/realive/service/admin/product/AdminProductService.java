package com.realive.service.admin.product;

import com.realive.dto.auction.AdminPurchaseRequestDTO;
import com.realive.dto.auction.AdminProductDTO;

public interface AdminProductService {
    
    /**
     * 관리자가 상품을 매입합니다.
     * 
     * @param requestDTO 매입 요청 정보
     * @param adminId 매입을 수행하는 관리자 ID
     * @return 매입된 상품 정보
     */
    AdminProductDTO purchaseProduct(AdminPurchaseRequestDTO requestDTO, Integer adminId);
    
    /**
     * 관리자 상품 정보를 조회합니다.
     * 
     * @param productId 조회할 상품 ID
     * @return 관리자 상품 정보
     */
    AdminProductDTO getAdminProduct(Integer productId);
} 