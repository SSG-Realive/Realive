package com.realive.repository.productview;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.QProduct;
import com.realive.domain.product.QProductImage;
import com.realive.dto.product.ProductListDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class ProductSearchImpl implements ProductSearch {
    
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductListDTO> productList(Pageable pageable) {

        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;

        int size = pageable.getPageSize();
        int offset = pageable.getPageNumber() * size;

        //데이터조회
        JPQLQuery<ProductListDTO> query = queryFactory
        .select(Projections.bean(ProductListDTO.class,
            product.id.as("id"),
            product.name.as("name"),
            product.price.as("price"),
            product.status.stringValue().as("status"),
            product.isActive.as("isActive"),
            productImage.url.as("thumbnailUrl")  // DTO의 thumbnailUrl에 매핑
        ))
        .from(product)
        .leftJoin(productImage)
        .on(productImage.product.eq(product)
            .and(productImage.isThumbnail.isTrue()))
        .limit(size)
        .offset(offset)
        .orderBy(new OrderSpecifier<>(Order.DESC, product.id));



        List<ProductListDTO> dtoList = query.fetch();

        return dtoList;
    }
}
