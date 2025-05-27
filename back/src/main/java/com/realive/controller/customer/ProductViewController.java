package com.realive.controller.customer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.CustomerProductSearchCondition;
import com.realive.dto.product.ProductListDTO;
import com.realive.service.product.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductService productService;

    /**
     * 구매자 전용 상품 목록 조회
     * - 필터링 조건: 카테고리, 가격 범위, 정렬 방식 등
     * - 페이징 처리
     */
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProductListDTO>> getVisibleProducts(
            @ModelAttribute CustomerProductSearchCondition cond) {
        return ResponseEntity.ok(productService.getVisibleProducts(cond));
    }
}
