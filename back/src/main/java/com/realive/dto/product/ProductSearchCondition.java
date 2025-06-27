package com.realive.dto.product;

import com.realive.domain.common.enums.ProductStatus;
import com.realive.dto.page.PageRequestDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 상품 검색/필터 조건 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSearchCondition extends PageRequestDTO{

    private Long categoryId;
    private ProductStatus status;
    private Integer minPrice;
    private Integer maxPrice;
    private Boolean isActive;
    
    // 중고 가구 특성에 맞는 재고 범위 필터
    private Integer minStock;  // 최소 재고
    private Integer maxStock;  // 최대 재고
    
    // 재고 상태별 필터 (편의용)
    private StockFilter stockFilter;
    
    public enum StockFilter {
        IN_STOCK("재고있음", 1, null),           // 1개 이상
        LOW_STOCK("재고부족", 1, 2),            // 1-2개
        SINGLE_ITEM("단일상품", 1, 1),          // 1개만
        MULTIPLE_ITEMS("다중상품", 2, null),    // 2개 이상
        LAST_ONE("마지막1개", 1, 1);            // 1개만 (긴급)
        
        private final String displayName;
        private final Integer min;
        private final Integer max;
        
        StockFilter(String displayName, Integer min, Integer max) {
            this.displayName = displayName;
            this.min = min;
            this.max = max;
        }
        
        public String getDisplayName() { return displayName; }
        public Integer getMin() { return min; }
        public Integer getMax() { return max; }
    }
}