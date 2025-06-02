package com.realive.controller.admin;

import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.auction.AdminPurchaseRequestDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.security.AdminPrincipal;
import com.realive.service.admin.product.AdminProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    /**
     * 관리자가 상품을 매입합니다.
     */
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<AdminProductDTO>> purchaseProduct(
            @RequestBody AdminPurchaseRequestDTO requestDTO,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        log.info("관리자 상품 매입 요청: adminId={}, productId={}, price={}", 
            adminPrincipal.getAdmin().getId(), requestDTO.getProductId(), requestDTO.getPurchasePrice());
            
        AdminProductDTO response = adminProductService.purchaseProduct(requestDTO, adminPrincipal.getAdmin().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 관리자가 매입한 상품 정보를 조회합니다.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<AdminProductDTO>> getAdminProduct(
            @PathVariable Integer productId,
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        log.info("관리자 상품 조회 요청: productId={}, adminId={}", productId, adminPrincipal.getAdmin().getId());
        AdminProductDTO response = adminProductService.getAdminProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
