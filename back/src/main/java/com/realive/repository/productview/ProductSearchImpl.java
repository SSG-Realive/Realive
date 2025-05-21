package com.realive.repository.productview;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.Product;
import com.realive.domain.product.QCategory;
import com.realive.domain.product.QProduct;
import com.realive.domain.product.QProductImage;
import com.realive.domain.seller.QSeller;
import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class ProductSearchImpl implements ProductSearch {

    private final JPAQueryFactory queryFactory;

    @Override
    public PageResponseDTO<ProductListDto> search(PageRequestDTO requestDTO, Long categoryId) {
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

        //하위 카테고리 포함한 필터링
        if (categoryId != null) {
            List<Long> categoryIds = findAllCategoryIdsIncludingChildren(categoryId);
            builder.and(product.category.id.in(categoryIds));
        }

        int offset = requestDTO.getOffset();
        int limit = requestDTO.getLimit();

        // 실제 데이터 조회
        // 데이터 조회 JPQLQuery 생성
        JPQLQuery<ProductListDto> query = queryFactory
            .select(Projections.bean(ProductListDto.class,
                product.id.as("id"),
                product.name.as("name"),
                product.price.as("price"),
                product.status.stringValue().as("status"), // enum일 경우 stringValue() 사용
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
            .where(builder)
            .offset(offset)
            .limit(limit)
            .orderBy(product.id.desc());

        // 리스트 조회
        List<ProductListDto> dtoList = query.fetch();

        // 전체 개수
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        return PageResponseDTO.<ProductListDto>withAll()
                .pageRequestDTO(requestDTO)
                .dtoList(dtoList)
                .total(total.intValue())
                .build();
    }

    // 예시: 카테고리 재귀 조회
    private List<Long> findAllCategoryIdsIncludingChildren(Long parentId) {
        // 실제로는 CategoryService 또는 Repository 통해 하위 ID 재귀적으로 조회
        // 예시에서는 간단하게 1개만
        return List.of(parentId);
    }
}
