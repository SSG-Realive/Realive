package com.realive.repository.customer.productview;

import java.util.Optional;

import com.realive.domain.product.QDeliveryPolicy;
import com.realive.dto.product.DeliveryPolicyDTO;
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
        QProductImage thumbnailImage = new QProductImage("thumbnailImage");
        QProductImage thumbnailVideo = new QProductImage("thumbnailVideo");
        QCategory category = QCategory.category;
        QSeller seller = QSeller.seller;
        QDeliveryPolicy deliveryPolicy = QDeliveryPolicy.deliveryPolicy;

        // DTO 직접 매핑용 임시 프로젝션 (배송 정책 필드를 분리해서 가져오기 위해)
        var result = queryFactory
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
                        seller.name.as("sellerName"),
                        seller.id.as("sellerId")
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
                .leftJoin(product.category, category)
                .leftJoin(product.seller, seller)
                .where(product.id.eq(id))
                .fetchOne();

        if (result == null) {
            return Optional.empty();
        }

        var deliveryPolicyResult = queryFactory
                .select(Projections.fields(DeliveryPolicyDTO.class,
                        deliveryPolicy.type,
                        deliveryPolicy.cost,
                        deliveryPolicy.regionLimit
                ))
                .from(deliveryPolicy)
                .where(deliveryPolicy.product.id.eq(id))
                .fetchOne();

        result.setDeliveryPolicy(deliveryPolicyResult);

        return Optional.of(result);

    }
}
