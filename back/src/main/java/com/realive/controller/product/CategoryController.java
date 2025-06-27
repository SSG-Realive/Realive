package com.realive.controller.product;

import com.realive.domain.product.Category;

import com.realive.dto.sellercategory.SellerCategoryDTO;
import com.realive.repository.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    // 전체 카테고리 목록 조회 (parent 포함해서 내려줌 → 계층형 드롭다운 구성 가능)
    @GetMapping
    public ResponseEntity<List<SellerCategoryDTO>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        List<SellerCategoryDTO> dtoList = categories.stream()
                .map(SellerCategoryDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(dtoList);
    }
}

