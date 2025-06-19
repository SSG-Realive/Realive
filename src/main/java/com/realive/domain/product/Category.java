package com.realive.domain.product;

import jakarta.persistence.*;
import lombok.*;

/**
 * 상품 카테고리 엔티티
 * 예: 침대, 소파, 책상 등
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카테고리 이름 (예: 소파, 침대 등)
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // 상위 카테고리 (자기 참조 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * 상위 카테고리 경로를 포함한 전체 경로 반환
     */
    public static String getCategoryFullPath(Category category) {
        if (category == null) return null;

        StringBuilder sb = new StringBuilder(category.getName());
        Category parent = category.getParent();

        // 무한 루프 방지용 (자기 참조 구조 보호)
        while (parent != null) {
            sb.insert(0, parent.getName() + " > ");
            parent = parent.getParent();
        }

        return sb.toString();
    }
}