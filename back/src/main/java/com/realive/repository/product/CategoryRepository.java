package com.realive.repository.product;

import com.realive.domain.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 카테고리 정보를 조회/저장하는 JPA Repository
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 추가로 필요한 경우 메서드 정의 가능
}