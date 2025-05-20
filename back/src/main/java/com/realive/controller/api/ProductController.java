package com.realive.controller.api;

import com.realive.dto.product.ProductRequestDto;
import com.realive.dto.product.ProductResponseDto;
import com.realive.domain.seller.Seller;
import com.realive.dto.product.ProductListDto;
import com.realive.security.JwtUtil;
import com.realive.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/products")
public class ProductController {

    private final ProductService productService;
    

    // 🔽 상품 등록록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createProduct(
            @ModelAttribute ProductRequestDto dto,
            @AuthenticationPrincipal Seller seller
    ) {
        Long sellerId = seller.getId();
        Long id = productService.createProduct(dto, sellerId);
        return ResponseEntity.ok(id);
    }

    // 🔽 상품 수정
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id,
            @ModelAttribute ProductRequestDto dto,
            @AuthenticationPrincipal Seller seller
    ) {
        Long sellerId = seller.getId();
        productService.updateProduct(id, dto, sellerId);
        return ResponseEntity.ok().build();
    }

    // 🔽 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal Seller seller
    ) {
        Long sellerId = seller.getId();
        productService.deleteProduct(id, sellerId);
        return ResponseEntity.ok().build();
    }

    // 🔽 상품 목록 조회 (판매자 전용)
    @GetMapping
    public ResponseEntity<List<ProductListDto>> getMyProducts(@AuthenticationPrincipal Seller seller) {
        Long sellerId = seller.getId();
        List<ProductListDto> list = productService.getProductsBySeller(sellerId);
        return ResponseEntity.ok(list);
    }

    // 🔽 단일 상품 상세 조회 (공개 API 가능)
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductDetail(@PathVariable Long id) {
        ProductResponseDto dto = productService.getProductDetail(id);
        return ResponseEntity.ok(dto);
    }
}