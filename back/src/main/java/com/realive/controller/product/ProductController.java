package com.realive.controller.product;

import com.realive.dto.product.ProductRequestDTO;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.dto.product.ProductSearchCondition;
import com.realive.domain.seller.Seller;
import com.realive.dto.page.PageResponseDTO;

import com.realive.dto.product.ProductListDTO;
import com.realive.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/products")
public class ProductController {

    private final ProductService productService;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    // 🔽 상품 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createProduct(@Valid @ModelAttribute ProductRequestDTO dto) {
        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long sellerId = seller.getId();

        Long id = productService.createProduct(dto, sellerId);
        return ResponseEntity.ok(id);
    }

    // 🔽 상품 수정
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @ModelAttribute ProductRequestDTO dto) {
        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long sellerId = seller.getId();

        productService.updateProduct(id, dto, sellerId);
        return ResponseEntity.ok().build();
    }

    // 🔽 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long sellerId = seller.getId();

        productService.deleteProduct(id, sellerId);
        return ResponseEntity.ok().build();
    }

    // 🔽 상품 목록 조회 (판매자 전용)
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProductListDTO>> getMyProducts(
            @ModelAttribute ProductSearchCondition condition) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        PageResponseDTO<ProductListDTO> response = productService.getProductsBySeller(email, condition);

        return ResponseEntity.ok(response);
    }

    // 🔽 단일 상품 상세 조회 (공개 API 가능)
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductDetail(@PathVariable Long id) {
        ProductResponseDTO dto = productService.getProductDetail(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * 관리자용 전체 상품 목록 조회
     * - 모든 판매자의 상품을 조회
     * - 필터링: 카테고리, 상태, 활성화 여부, 가격 범위, 키워드 검색
     */
    @GetMapping("/admin/products")
    public ResponseEntity<PageResponseDTO<ProductListDTO>> getAllProductsForAdmin(
            @ModelAttribute ProductSearchCondition condition) {
        log.info("관리자용 전체 상품 목록 조회 요청 - 조건: {}", condition);
        PageResponseDTO<ProductListDTO> response = productService.getAllProductsForAdmin(condition);
        return ResponseEntity.ok(response);
    }
}