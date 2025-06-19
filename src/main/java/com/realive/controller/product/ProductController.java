package com.realive.controller.product;

import com.realive.dto.product.ProductRequestDTO;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.dto.product.ProductSearchCondition;
import com.realive.security.seller.SellerPrincipal;
import com.realive.domain.seller.Seller;
import com.realive.dto.page.PageResponseDTO;

import com.realive.dto.product.ProductListDTO;
import com.realive.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createProduct(@Valid @ModelAttribute ProductRequestDTO dto, @AuthenticationPrincipal SellerPrincipal principal) {
        
        Long sellerId = principal.getId();

        Long id = productService.createProduct(dto, sellerId);
        return ResponseEntity.ok(id);
    }

    // 🔽 상품 수정
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @ModelAttribute ProductRequestDTO dto, @AuthenticationPrincipal SellerPrincipal principal) {
       
        Long sellerId = principal.getId();

        productService.updateProduct(id, dto, sellerId);
        return ResponseEntity.ok().build();
    }

    // 🔽 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id,  @AuthenticationPrincipal SellerPrincipal principal) {
        
        Long sellerId = principal.getId();

        productService.deleteProduct(id, sellerId);
        return ResponseEntity.ok().build();
    }

    // 🔽 상품 목록 조회 (판매자 전용)
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProductListDTO>> getMyProducts(
            @ModelAttribute ProductSearchCondition condition,
            @AuthenticationPrincipal SellerPrincipal principal) {

        Long sellerId = principal.getId();

        PageResponseDTO<ProductListDTO> response = productService.getProductsBySeller(sellerId, condition);

        return ResponseEntity.ok(response);
    }

    // 🔽 단일 상품 상세 조회 (공개 API 가능)
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductDetail(@PathVariable Long id, @AuthenticationPrincipal SellerPrincipal principal) {
        
        Long sellerId = principal.getId();
        ProductResponseDTO dto = productService.getProductDetail(id, sellerId);
        return ResponseEntity.ok(dto);
    }
}