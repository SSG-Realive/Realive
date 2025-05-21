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

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(ProductSearchCondition cond, Long sellerId) {
        
        QProduct product = QProduct.product;
        Pageable pageable = cond.toPageable();
        
        BooleanBuilder builder = buildConditon(product, cond, sellerId);

        List<Product> content = queryFactory
                .selectFrom(product)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();
        
        Long toatl = queryFactory
                .select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, toatl != null ? toatl : 0L);
    }

    private BooleanBuilder buildConditon(QProduct product, ProductSearchCondition cond, Long sellerId){
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(product.seller.id.eq(sellerId));

       if (cond.getIsActive() != null) {
            builder.and(product.active.eq(cond.getIsActive()));
        }

        if (cond.getStatus() != null) {
            builder.and(product.status.eq(cond.getStatus()));
        }

        if (cond.getCategoryId() != null) {
            builder.and(product.category.id.eq(cond.getCategoryId()));
        }

        if (cond.getKeyword() != null && !cond.getKeyword().isBlank()) {
            builder.and(product.name.containsIgnoreCase(cond.getKeyword()));
        }

        if (cond.getMinPrice() != null) {
            builder.and(product.price.goe(cond.getMinPrice()));
        }

        if (cond.getMaxPrice() != null) {
            builder.and(product.price.loe(cond.getMaxPrice()));
        }

        return builder;
    }
}




