package com.realive.repository.customer.productview;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.QCategory;
import com.realive.domain.product.QProduct;
import com.realive.domain.product.QProductImage;
import com.realive.domain.seller.QSeller;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.domain.common.enums.MediaType;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductDetailImpl implements ProductDetail {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ProductResponseDTO> findProductDetailById(Long id) {

        QProduct product = QProduct.product;
        QProductImage thumbnailImage = new QProductImage("thumbnailImage"); // 대표 이미지
        QProductImage thumbnailVideo = new QProductImage("thumbnailVideo"); // 대표 영상
        QCategory category = QCategory.category;
        QSeller seller = QSeller.seller;

        ProductResponseDTO dto = queryFactory
                .select(Projections.fields(ProductResponseDTO.class,
                        product.id.as("id"),
                        product.name.as("name"),
                        product.description.as("description"),
                        product.price.as("price"),
                        product.stock.as("stock"),
                        product.width.as("width"),
                        product.depth.as("depth"),
                        product.height.as("height"),
                        product.status.stringValue().as("status"),
                        product.active.as("isActive"),
                        thumbnailImage.url.as("imageThumbnailUrl"),
                        thumbnailVideo.url.as("videoThumbnailUrl"),
                        category.name.as("categoryName"),
                        category.id.as("categoryId"),
                        category.parent.id.as("parentCategoryId"),
                        seller.name.as("sellerName")
                ))
                .from(product)
                .leftJoin(thumbnailImage).on(
                        thumbnailImage.product.eq(product)
                                .and(thumbnailImage.isThumbnail.isTrue())
                                .and(thumbnailImage.mediaType.eq(MediaType.IMAGE))
                )
                .leftJoin(thumbnailVideo).on(
                        thumbnailVideo.product.eq(product)
                                .and(thumbnailVideo.isThumbnail.isTrue())
                                .and(thumbnailVideo.mediaType.eq(MediaType.VIDEO))
                )
                .leftJoin(category).on(product.category.eq(category))
                .leftJoin(seller).on(product.seller.eq(seller))
                .where(product.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(dto);
    }
}
