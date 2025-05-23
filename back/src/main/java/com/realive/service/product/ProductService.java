package com.realive.service.product;

import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductRequestDTO;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.dto.product.ProductSearchCondition;

import java.util.List;

public interface ProductService {

    /**
     * 상품 등록
     * - imageThumbnail: 대표 이미지 (필수)
     * - videoThumbnail: 대표 영상 (선택)
     */
    Long createProduct(ProductRequestDTO dto, Long sellerId);

    /**
     * 상품 수정
     * - 기존 썸네일 이미지/영상 모두 삭제 후 새로 저장
     */
    void updateProduct(Long productId, ProductRequestDTO dto, Long sellerId);

    /**
     * 상품 삭제 (이미지, 배송정책 포함 삭제)
     */
    void deleteProduct(Long productId, Long sellerId);

    /**
     * 판매자 ID 기준 상품 목록 조회
     * - imageThumbnailUrl / videoThumbnailUrl 포함
     */
    PageResponseDTO<ProductListDTO> getProductsBySeller(String email, ProductSearchCondition condition);

    /**
     * 상품 상세 조회
     * - imageThumbnailUrl / videoThumbnailUrl 포함
     */
    ProductResponseDTO getProductDetail(Long productId);

}