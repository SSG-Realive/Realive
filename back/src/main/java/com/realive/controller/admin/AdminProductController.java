package com.realive.controller.admin;


import com.realive.dto.product.ProductListDTO;
import com.realive.dto.admin.ProductDetailDTO;
import com.realive.service.admin.product.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    // 전체 상품 목록 조회 (필터: 판매자, 검색어, 활성화 상태)
    @GetMapping
    public ResponseEntity<Page<ProductListDTO>> getProducts(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return ResponseEntity.ok(
                adminProductService.getProducts(sellerId, search, active, pageable)
        );
    }

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailDTO> getProductDetails(@PathVariable Long productId) {
        // 이미지 썸네일 등 필요한 경우, 서비스에서 처리
        return ResponseEntity.ok(
                adminProductService.getProductDetails(productId)
        );
    }

    // 상품 비활성화(삭제 대체)
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long productId) {
        adminProductService.deactivateProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // 예외 처리 (선택)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
