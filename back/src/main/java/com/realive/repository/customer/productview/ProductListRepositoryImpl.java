package com.realive.repository.customer.productview;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.realive.domain.product.QCategory;
import com.realive.domain.product.QProduct;
import com.realive.domain.product.QProductImage;
import com.realive.domain.seller.QSeller;
import com.realive.dto.product.ProductListDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
@RequiredArgsConstructor
public class ProductListRepositoryImpl implements ProductListRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductListDTO> getWishlistedProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;
        QSeller seller = QSeller.seller;
        QCategory category = QCategory.category;
      

        return queryFactory
            .select(Projections.bean(ProductListDTO.class,
                product.id.as("id"),
                product.name.as("name"),
                product.price.as("price"),
                product.status.stringValue().as("status"),
                product.isActive.as("isActive"),
                productImage.url.as("thumbnailUrl"),
                seller.name.as("sellerName"),
                category.name.as("categoryName")
            ))
            .from(product)
            .leftJoin(productImage)
                .on(productImage.product.eq(product)
                    .and(productImage.isThumbnail.isTrue()))
            .leftJoin(product.seller, seller)
            .leftJoin(product.category, category)
            .where(product.id.in(productIds))
            .fetch();
    }

    
}
