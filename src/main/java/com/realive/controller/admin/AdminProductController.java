package com.realive.controller.admin;

import com.realive.domain.product.Category;
import com.realive.dto.admin.ProductDetailDTO;
import com.realive.dto.auction.AdminPurchaseRequestDTO;
import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductSearchCondition;
import com.realive.security.AdminPrincipal;
import com.realive.service.product.ProductService;
import com.realive.service.admin.product.AdminProductService;
import com.realive.repository.product.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminProductController {

    private final ProductService productService;
    private final AdminProductService adminProductService;
    private final CategoryRepository categoryRepository;
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
     * 카테고리 목록 조회
     * - 모든 카테고리를 조회
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        log.info("카테고리 목록 조회 요청");
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    /**
     * 관리자 상품 매입
     * - 관리자가 판매자의 상품을 매입
     * - 상품 상태, 재고 확인
     * - 중복 매입 방지
     */
    @PostMapping("/products/purchase")
    public ResponseEntity<ApiResponse<AdminProductDTO>> purchaseProduct(
            @Valid @RequestBody AdminPurchaseRequestDTO requestDTO,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        log.info("관리자 상품 매입 요청 - adminId: {}, productId: {}, price: {}",
                adminPrincipal.getAdmin().getId(), requestDTO.getProductId(), requestDTO.getPurchasePrice());

        try {
            AdminProductDTO response = adminProductService.purchaseProduct(
                    requestDTO,
                    adminPrincipal.getAdmin().getId()
            );
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
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