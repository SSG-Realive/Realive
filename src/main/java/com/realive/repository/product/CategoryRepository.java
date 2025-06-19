package com.realive.repository.product;

import com.realive.domain.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 카테고리 정보를 조회/저장하는 JPA Repository
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 선택된 카테고리와 그 하위 카테고리의 ID를 모두 반환
     * 예: categoryId = 1 (가구) → [1, 10, 11, 12, 13]
     */
    @Query("SELECT c.id FROM Category c WHERE c.id = :categoryId OR c.parent.id = :categoryId")
    List<Long> findSubCategoryIdsIncludingSelf(@Param("categoryId") Long categoryId);

    // 추가 메서드가 필요한 경우 여기에 정의
}
