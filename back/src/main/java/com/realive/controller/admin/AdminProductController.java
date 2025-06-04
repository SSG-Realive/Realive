package com.realive.controller.admin;

import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductSearchCondition;
import com.realive.service.product.ProductService;
import com.realive.service.admin.product.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminProductController {

    private final ProductService productService;
    private final AdminProductService adminProductService;
    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

    /**
     * 관리자용 전체 상품 목록 조회
     * - 모든 판매자의 상품을 조회
     * - 필터링: 카테고리, 상태, 활성화 여부, 가격 범위, 키워드 검색
     */
    @GetMapping("/products")
    public ResponseEntity<PageResponseDTO<ProductListDTO>> getAllProducts(
            @ModelAttribute ProductSearchCondition condition) {
        log.info("관리자용 전체 상품 목록 조회 요청 - 조건: {}", condition);
        PageResponseDTO<ProductListDTO> response = productService.getAllProductsForAdmin(condition);
        return ResponseEntity.ok(response);
    }

    /**
     * 관리자 물품 목록 조회
     * - 관리자가 매입한 물품 목록을 조회
     * - 필터링: 카테고리, 경매 상태, 가격 범위, 키워드 검색
     */
    @GetMapping("/owned-products")
    public ResponseEntity<PageResponseDTO<ProductListDTO>> getAdminProducts(
            @ModelAttribute ProductSearchCondition condition) {
        log.info("관리자 물품 목록 조회 요청 - 조건: {}", condition);
        PageResponseDTO<ProductListDTO> response = adminProductService.getAdminProducts(condition);
        return ResponseEntity.ok(response);
    }
} 