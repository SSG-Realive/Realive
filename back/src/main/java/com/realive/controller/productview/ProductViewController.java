package com.realive.controller.productview;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.product.ProductListDto;
import com.realive.dto.product.ProductResponseDto;
import com.realive.service.ProductViewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/realive/main")
@Log4j2
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductViewService productViewService;

    @GetMapping
    public ResponseEntity<Page<ProductListDto>> getProducts(Pageable pageable) {
        Page<ProductListDto> result = productViewService.getProductList(pageable);
        return ResponseEntity.ok(result);
    }   

    // 상품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductDetail(@PathVariable("id") Long id) {
        ProductResponseDto productDetail = productViewService.getProductDetail(id);
        return ResponseEntity.ok(productDetail);
    }
    
}
