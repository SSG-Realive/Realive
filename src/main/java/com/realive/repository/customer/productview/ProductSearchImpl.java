package com.realive.repository.customer.productview;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.QCategory;
import com.realive.domain.product.QProduct;
import com.realive.domain.product.QProductImage;
import com.realive.domain.seller.QSeller;
import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.stream.Collectors;


@Log4j2
@RequiredArgsConstructor
@Repository
public class ProductSearchImpl implements ProductSearch {

    private final JPAQueryFactory queryFactory;

    public List<Long> findSubCategoryIdsIncludingSelf(Long categoryId) {
        if (categoryId == null) {
            return Collections.emptyList();
        }

        QCategory category = QCategory.category;

        List<Long> categoryIds = queryFactory
                .select(category.id)
                .from(category)
                .where(category.id.eq(categoryId)
                        .or(category.parent.id.eq(categoryId)))
                .fetch();

        if (!categoryIds.contains(categoryId)) {
            categoryIds.add(categoryId);
        }

        return categoryIds;
    }

    @Override
    public PageResponseDTO<ProductListDTO> search(PageRequestDTO requestDTO, Long categoryId) {
        QProduct product = QProduct.product;
        QCategory category = QCategory.category;
        QProductImage productImage = QProductImage.productImage;
        QSeller seller = QSeller.seller;

        BooleanBuilder builder = new BooleanBuilder();

        String keyword = requestDTO.getKeyword();
        String[] types = requestDTO.getType() != null ? requestDTO.getType().split("") : new String[]{};

        if (keyword != null && !keyword.isBlank()) {
            BooleanBuilder keywordBuilder = new BooleanBuilder();
            for (String type : types) {
                switch (type) {
                    case "T" -> keywordBuilder.or(product.name.containsIgnoreCase(keyword));
                    case "S" -> keywordBuilder.or(product.seller.name.containsIgnoreCase(keyword));
                    case "C" -> keywordBuilder.or(product.category.name.containsIgnoreCase(keyword));
                }
            }
            builder.and(keywordBuilder);
        }

        if (categoryId != null) {
            List<Long> categoryIds = findSubCategoryIdsIncludingSelf(categoryId);
            log.info("📂 포함된 카테고리 ID 목록: {}", categoryIds);
            builder.and(product.category.id.in(categoryIds));
        } else {
            log.info("📂 전체 카테고리 대상 조회");
        }

        int offset = requestDTO.getOffset();
        int limit = requestDTO.getLimit();

        JPQLQuery<Tuple> productQuery = queryFactory
                .select(
                        product.id,
                        product.name,
                        product.price,
                        product.status,
                        product.active,
                        product.stock,
                        seller.id,
                        seller.name,
                        category.name,
                        category.parent.name
                )
                .from(product)
                .leftJoin(product.seller, seller)
                .leftJoin(product.category, category)
                .where(builder)
                .offset(offset)
                .limit(limit)
                .orderBy(product.id.desc());

        List<Tuple> productTuples = productQuery.fetch();

        List<Long> productIds = productTuples.stream()
                .map(t -> t.get(product.id))
                .toList();

        Map<Long, String> imageMap = productIds.isEmpty() ? new HashMap<>() :
                queryFactory
                        .select(productImage.product.id, productImage.url)
                        .from(productImage)
                        .where(productImage.product.id.in(productIds)
                                .and(productImage.isThumbnail.isTrue()))
                        .fetch()
                        .stream()
                        .collect(Collectors.toMap(
                                row -> row.get(productImage.product.id),
                                row -> row.get(productImage.url),
                                (existing, replacement) -> existing
                        ));

        List<ProductListDTO> dtoList = productTuples.stream()
                .map(row -> ProductListDTO.builder()
                        .id(row.get(product.id))
                        .name(row.get(product.name))
                        .price(row.get(product.price))
                        .status(row.get(product.status).name())
                        .isActive(row.get(product.active))
                        .stock(row.get(product.stock))
                        .sellerId(row.get(seller.id))
                        .sellerName(row.get(seller.name))
                        .categoryName(row.get(category.name))
                        .parentCategoryName(row.get(category.parent.name))
                        .imageThumbnailUrl(imageMap.get(row.get(product.id)))
                        .build())
                .toList();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        return PageResponseDTO.<ProductListDTO>withAll()
                .pageRequestDTO(requestDTO)
                .dtoList(dtoList)
                .total(total != null ? total.intValue() : 0)
                .build();
    }
}