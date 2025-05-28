package com.realive.repository.product;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.Product;
import com.realive.domain.product.QCategory;
import com.realive.domain.product.QProduct;
import com.realive.domain.seller.QSeller;
import com.realive.dto.product.CustomerProductSearchCondition;
import com.realive.dto.product.ProductSearchCondition;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ProductRepositoryImpl
 * - ProductRepositoryCustom 인터페이스의 구현체
 * - QueryDSL을 활용하여 복합 조건 기반의 상품 검색 기능 제공
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 검색 조건과 판매자 ID를 기반으로 상품 목록을 조회하여 페이징 처리된 결과 반환
     *
     * @param cond      검색 조건 DTO
     * @param sellerId  현재 로그인한 판매자의 ID
     * @return          조건에 맞는 상품 페이지 결과
     */
    @Override
    public Page<Product> searchProducts(ProductSearchCondition cond, Long sellerId) {
        QProduct product = QProduct.product;
        Pageable pageable = cond.toPageable();

        // 조건 생성
        BooleanBuilder builder = buildConditon(product, cond, sellerId);

        // 조건에 맞는 실제 데이터 조회
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
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
     * 검색 조건들을 BooleanBuilder에 누적하여 QueryDSL 조건 빌더 생성
     *
     * @param product   QueryDSL QProduct 객체
     * @param cond      검색 조건 DTO
     * @param sellerId  현재 로그인한 판매자의 ID
     * @return          BooleanBuilder (검색 조건 누적)
     */
    private BooleanBuilder buildConditon(QProduct product, ProductSearchCondition cond, Long sellerId){
        BooleanBuilder builder = new BooleanBuilder();

        // 판매자 ID 조건 (필수)
        builder.and(product.seller.id.eq(sellerId));

        // 활성 여부 조건
        if (cond.getIsActive() != null) {
            builder.and(product.isActive.eq(cond.getIsActive()));
        }

        // 상품 상태 조건
        if (cond.getStatus() != null) {
            builder.and(product.status.eq(cond.getStatus()));
        }

        // 카테고리 조건
        if (cond.getCategoryId() != null) {
            builder.and(product.category.id.eq(cond.getCategoryId()));
        }

        // 키워드 검색 (상품명)
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
    //구매자 전용 물품 리스트
    @Override
    public Page<Product> searchVisibleProducts(CustomerProductSearchCondition condition) {
        QProduct product = QProduct.product;
        QCategory category = QCategory.category;
        QSeller seller = QSeller.seller;

        BooleanBuilder builder = new BooleanBuilder();

        //활성 상품만 표기할 것
        builder.and(product.isActive.eq(true));

        //키워드 검색
        if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
            builder.and(
                product.name.containsIgnoreCase(condition.getKeyword())
                .or(product.description.containsIgnoreCase(condition.getKeyword()))
            );
        }
        //카테고리 조건
        if (condition.getCategoryId() !=null) {
            builder.and(product.category.id.eq(condition.getCategoryId()));
        } else if (condition.getParentCategoryId() != null) {
            builder.and(product.category.parent.id.eq(condition.getParentCategoryId()));
            
        }// end if

        //판매자 이름 조건
        if (condition.getSellerName() !=null && !condition.getSellerName().isBlank()){
            builder.and(product.seller.name.containsIgnoreCase(condition.getSellerName()));
        } 
        
        //가격 조건
        if (condition.getMinPrice() != null) {
            builder.and(product.price.goe(condition.getMinPrice()));
        }
        if (condition.getMaxPrice() != null) {
            builder.and(product.price.loe(condition.getMaxPrice()));            
        }

        //정렬 조건 처리
        OrderSpecifier<?> orderSpec = product.createdAt.desc(); // 기본값
        if (condition.getSort() != null) {
            switch (condition.getSort()) {
                case "price_asc" -> orderSpec = product.price.asc();
                case "price_desc" -> orderSpec = product.price.desc();
                case "new" -> orderSpec = product.createdAt.desc();
            }
        }

        // 페이징 처리
        JPAQuery<Product> query = queryFactory.selectFrom(product)
                .leftJoin(product.category, category).fetchJoin()
                .leftJoin(product.seller, seller).fetchJoin()
                .where(builder)
                .orderBy(orderSpec)
                .offset(condition.getPageIndex() * condition.getSize())
                .limit(condition.getSize());

        List<Product> resultList = query.fetch();

        long total = queryFactory.select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(resultList, condition.toPageable(), total);
            
        }
}
            
        
