package com.realive.repository.customer.productview;

import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.QCategory;
import com.realive.domain.product.QProduct;
import com.realive.domain.product.QProductImage;
import com.realive.domain.seller.QSeller;
import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.repository.product.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

@Log4j2
@RequiredArgsConstructor
@Repository
public class ProductSearchImpl implements ProductSearch {

    private final JPAQueryFactory queryFactory;
    private final CategoryRepository categoryRepository; // ✅ 추가

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

        // ✅ 하위 카테고리까지 포함
        if (categoryId != null) {
            List<Long> categoryIds = categoryRepository.findSubCategoryIdsIncludingSelf(categoryId);
            log.info("📂 포함된 카테고리 ID 목록: {}", categoryIds);
            builder.and(product.category.id.in(categoryIds));
        }

        int offset = requestDTO.getOffset();
        int limit = requestDTO.getLimit();

        JPQLQuery<ProductListDTO> query = queryFactory
                .select(Projections.bean(ProductListDTO.class,
                        product.id.as("id"),
                        product.name.as("name"),
                        product.price.as("price"),
                        product.status.stringValue().as("status"),
                        product.active.as("isActive"),
                        productImage.url.as("imageThumbnailUrl"),
                        seller.name.as("sellerName"),
                        category.name.as("categoryName")
                ))
                .from(product)
                .leftJoin(productImage)
                .on(productImage.product.eq(product)
                        .and(productImage.isThumbnail.isTrue()))
                .leftJoin(product.seller, seller)
                .leftJoin(product.category, category)
                .where(builder)
                .offset(offset)
                .limit(limit)
                .orderBy(product.id.desc());

        List<ProductListDTO> dtoList = query.fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        return PageResponseDTO.<ProductListDTO>withAll()
                .pageRequestDTO(requestDTO)
                .dtoList(dtoList)
                .total(total.intValue())
                .build();
    }

    // ✅ 기존 함수 삭제 (불필요)
}
