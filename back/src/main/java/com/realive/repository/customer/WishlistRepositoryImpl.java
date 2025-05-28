package com.realive.repository.customer;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.QProduct;
import com.realive.domain.product.QProductImage;
import com.realive.domain.customer.QWishlist;
import com.realive.dto.wishlist.WishlistMostResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WishlistRepositoryImpl implements WishlistRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<WishlistMostResponseDTO> findTopNWishlistedProducts(int limit) {
        QWishlist wishlist = QWishlist.wishlist;
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;

        return queryFactory
                .select(Projections.constructor(WishlistMostResponseDTO.class,
                        product.id,
                        wishlist.id.countDistinct(),
                        product.name, // DTO의 productName 필드에 매핑
                        // 썸네일 이미지 URL을 가져오는 서브쿼리 부분
                        JPAExpressions.select(productImage.url) // 서브쿼리 결과 자체가 String 타입
                                .from(productImage)
                                .where(productImage.product.eq(product)
                                        .and(productImage.isThumbnail.isTrue())) // ProductImage에 isThumbnail 필드 있다고 가정
                                .limit(1) // 썸네일 1개
                ))
                .from(wishlist)
                .join(wishlist.product, product)
                .groupBy(product.id, product.name)
                .orderBy(wishlist.id.countDistinct().desc())
                .limit(limit)
                .fetch();
    }
}