package com.realive.dto.sellercategory;

import com.realive.domain.product.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerCategoryDTO {

     private Long id;
    private String name;
    private Long parentId;  // 계층 구조 지원용

    public static SellerCategoryDTO fromEntity(Category category) {
        return new SellerCategoryDTO(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null
        );
    }
    
}
