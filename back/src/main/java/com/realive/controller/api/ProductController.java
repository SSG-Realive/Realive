package com.realive.controller.api;

import com.realive.dto.product.ProductRequestDto;
import com.realive.dto.product.ProductResponseDto;
import com.realive.exception.UnauthorizedException;
import com.realive.dto.product.ProductListDto;
import com.realive.security.JwtUtil;
import com.realive.service.product.ProductService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/products")
public class ProductController {

    private final ProductService productService;
    private final JwtUtil jwtUtil;

    // 🔽 상품 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createProduct(
            @ModelAttribute ProductRequestDto dto,
            HttpServletRequest request
    ) {
        String token = jwtUtil.resolveToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("토큰이 유효하지 않습니다.");
        }

        Claims claims = jwtUtil.getClaims(token);
        Long sellerId = claims.get("id", Long.class);

        Long id = productService.createProduct(dto, sellerId);
        return ResponseEntity.ok(id);
    }

    // 🔽 상품 수정
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id,
            @ModelAttribute ProductRequestDto dto,
            HttpServletRequest request
    ) {
        String token = jwtUtil.resolveToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("토큰이 유효하지 않습니다.");
        }
        Claims claims = jwtUtil.getClaims(token);
        Long sellerId = claims.get("id", Long.class);

        productService.updateProduct(id, dto, sellerId);
        return ResponseEntity.ok().build();
    }

    // 🔽 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String token = jwtUtil.resolveToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("토큰이 유효하지 않습니다.");
        }
        Claims claims = jwtUtil.getClaims(token);
        Long sellerId = claims.get("id", Long.class);

        productService.deleteProduct(id, sellerId);
        return ResponseEntity.ok().build();
    }

    // 🔽 상품 목록 조회 (판매자 전용)
    @GetMapping
    public ResponseEntity<List<ProductListDto>> getMyProducts(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("토큰이 유효하지 않습니다.");
        }
        
        Claims claims = jwtUtil.getClaims(token);
        Long sellerId = claims.get("id", Long.class);

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