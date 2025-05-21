package com.realive.repository.product;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.Product;
import com.realive.domain.product.QProduct;
import com.realive.dto.product.ProductSearchCondition;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ProductRepositoryCustom의 구현 클래스
 * - 상품 검색 기능 (동적 조건 포함)을 QueryDSL 기반으로 구현
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    // QueryDSL의 JPA 쿼리 빌더 (DI)
    private final JPAQueryFactory queryFactory;

    /**
     * 조건 기반으로 판매자의 상품을 검색 (페이징 포함)
     *
     * @param cond 상품 검색 조건 DTO
     * @param sellerId 해당 판매자 ID
     * @return 검색 결과 Page 객체
     */
    @Override
    public Page<Product> searchProducts(ProductSearchCondition cond, Long sellerId) {

        QProduct product = QProduct.product;
        Pageable pageable = cond.toPageable(); // 요청에서 Pageable 생성

        // 검색 조건 빌드
        BooleanBuilder builder = buildConditon(product, cond, sellerId);

        // 실제 데이터 목록 조회
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(builder)
                .offset(pageable.getOffset()) // 시작 위치
                .limit(pageable.getPageSize()) // 페이지 크기
                .orderBy(product.createdAt.desc()) // 최신순 정렬
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 검색 조건 동적 빌더 생성
     *
     * @param product Q타입 Product
     * @param cond 검색 조건
     * @param sellerId 판매자 ID
     * @return BooleanBuilder
     */
    private BooleanBuilder buildConditon(QProduct product, ProductSearchCondition cond, Long sellerId){
        BooleanBuilder builder = new BooleanBuilder();

        // 해당 판매자의 상품만 검색
        builder.and(product.seller.id.eq(sellerId));

        // 판매 상태 조건
        if (cond.getIsActive() != null) {
            builder.and(product.active.eq(cond.getIsActive()));
        }

        // 상품 상태 조건 (예: SALE, SOLD_OUT 등)
        if (cond.getStatus() != null) {
            builder.and(product.status.eq(cond.getStatus()));
        }

        // 카테고리 조건
        if (cond.getCategoryId() != null) {
            builder.and(product.category.id.eq(cond.getCategoryId()));
        }

        // 키워드 검색 (상품 이름 기준, 대소문자 무시)
        if (cond.getKeyword() != null && !cond.getKeyword().isBlank()) {
            builder.and(product.name.containsIgnoreCase(cond.getKeyword()));
        }

        // 최소 가격 조건
        if (cond.getMinPrice() != null) {
            builder.and(product.price.goe(cond.getMinPrice()));
        }

        // 최대 가격 조건
        if (cond.getMaxPrice() != null) {
            builder.and(product.price.loe(cond.getMaxPrice()));
        }

        return builder;
    }
}