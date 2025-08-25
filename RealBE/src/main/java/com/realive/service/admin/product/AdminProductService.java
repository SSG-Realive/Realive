package com.realive.service.admin.product;

import com.realive.dto.admin.ProductDetailDTO;
import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.auction.AdminPurchaseRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

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

    /**
     * 관리자 물품의 전체 목록을 페이징하여 조회합니다.
     * @param pageable 페이징 및 정렬 정보
     * @param categoryFilter 카테고리 필터 (선택사항)
     * @param isAuctioned 경매 등록 여부 필터 (선택사항)
     * @return 페이징된 AdminProductDTO 목록
     */
    Page<AdminProductDTO> getAllAdminProducts(Pageable pageable, String categoryFilter, Boolean isAuctioned);

    /**
     * 특정 관리자 물품의 상세 정보를 조회합니다.
     * @param adminProductId 조회할 관리자 물품의 ID
     * @return AdminProductDTO (없으면 Optional.empty())
     */
    Optional<AdminProductDTO> getAdminProductDetails(Integer adminProductId);

    /**
     * 관리자 물품 목록을 조회합니다.
     * @param condition 검색 조건
     * @return 페이징된 ProductListDTO 목록
     */
    PageResponseDTO<ProductListDTO> getAdminProducts(ProductSearchCondition condition);

    // 상품 상세 조회
    ProductDetailDTO getProductDetails(Long productId);

    // 상품 비활성화 (삭제 대체)
    void deactivateProduct(Long productId);

} 