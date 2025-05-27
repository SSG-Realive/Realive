package com.realive.controller.productview;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.service.customer.ProductViewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/items")
@Log4j2
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductViewService productViewService;

    //검색
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProductListDTO>> list(
            @ModelAttribute PageRequestDTO pageRequestDTO,
            @RequestParam(name = "categoryId", required = false) Long categoryId) {

        log.info("page: {}, size: {}, offset: {}", pageRequestDTO.getPage(), pageRequestDTO.getSize(), pageRequestDTO.getOffset());

        PageResponseDTO<ProductListDTO> result = productViewService.search(pageRequestDTO, categoryId);
        return ResponseEntity.ok(result);
    }   

    // 상품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductDetail(@PathVariable("id") Long id) {
        ProductResponseDTO productDetail = productViewService.getProductDetail(id);
        return ResponseEntity.ok(productDetail);
    }

    
}
