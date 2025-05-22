package com.realive.controller.productview;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.service.ProductViewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/items")
@Log4j2
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductViewService productViewService;

    @GetMapping
    public ResponseEntity<Page<ProductListDTO>> getProducts(Pageable pageable) {
        Page<ProductListDTO> result = productViewService.getProductList(pageable);
        return ResponseEntity.ok(result);
    }   

    // 상품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductDetail(@PathVariable("id") Long id) {
        ProductResponseDTO productDetail = productViewService.getProductDetail(id);
        return ResponseEntity.ok(productDetail);
    }
    
}
