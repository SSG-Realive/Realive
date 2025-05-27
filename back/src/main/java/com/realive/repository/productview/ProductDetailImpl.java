package com.realive.repository.productview;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.QCategory;
import com.realive.domain.product.QProduct;
import com.realive.domain.product.QProductImage;
import com.realive.domain.seller.QSeller;
import com.realive.dto.product.ProductResponseDTO;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductDetailImpl implements ProductDetail {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ProductResponseDTO> findProductDetailById(Long id) {

        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;
        QCategory category = QCategory.category;
        QSeller seller = QSeller.seller;

        ProductResponseDTO dto = queryFactory
            .select(Projections.constructor(ProductResponseDTO.class,
                product.id,
                product.name,
                product.description,
                product.price,
                product.stock,
                product.width,
                product.depth,
                product.height,
                product.status.stringValue(),
                product.isActive,
                productImage.url,
                category.name,
                seller.name
            ))
            .from(product)
            .leftJoin(productImage)
                .on(productImage.product.eq(product)
                    .and(productImage.isThumbnail.isTrue()))
            .leftJoin(category).on(product.category.eq(category))
            .leftJoin(seller).on(product.seller.eq(seller))
            .where(product.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(dto);
    }
    
}
