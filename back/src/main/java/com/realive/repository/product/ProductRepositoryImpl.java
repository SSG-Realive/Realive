package com.realive.repository.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.Product;
import com.realive.domain.product.QProduct;
import com.realive.dto.product.ProductSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Product> searchProducts(ProductSearchCondition cond, Long sellerId) {
        QProduct product = QProduct.product;

        return queryFactory.selectFrom(product)
                .where(
                        product.seller.id.eq(sellerId),
                        StringUtils.hasText(cond.getKeyword()) ? product.name.contains(cond.getKeyword()) : null,
                        cond.getCategoryId() != null ? product.category.id.eq(cond.getCategoryId()) : null,
                        cond.getStatus() != null ? product.status.stringValue().eq(cond.getStatus()) : null,
                        cond.getMinPrice() != null ? product.price.goe(cond.getMinPrice()) : null,
                        cond.getMaxPrice() != null ? product.price.loe(cond.getMaxPrice()) : null,
                        cond.getIsActive() != null ? product.isActive.eq(cond.getIsActive()) : null
                )
                .orderBy(product.createdAt.desc()) // 최신 등록 순
                .fetch();
    }
}