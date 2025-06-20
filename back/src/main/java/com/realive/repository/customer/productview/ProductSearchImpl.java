package com.realive.repository.customer.productview;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
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

        // 1. 상품 목록 조회 (이미지 제외)
        List<Tuple> productRows = queryFactory
                .select(
                        product.id,
                        product.name,
                        product.price,
                        product.status,
                        product.active,
                        seller.name,
                        category.name
                )
                .from(product)
                .leftJoin(product.seller, seller)
                .leftJoin(product.category, category)
                .where(builder)
                .offset(offset)
                .limit(limit)
                .orderBy(product.id.desc())
                .fetch();

        // 2. 상품 ID 목록 추출
        List<Long> productIds = productRows.stream()
                .map(row -> row.get(product.id))
                .toList();

        // 3. 썸네일 이미지 URL 조회
        final Map<Long, String> imageMap;
        if (!productIds.isEmpty()) {
            List<Tuple> imageRows = queryFactory
                    .select(productImage.product.id, productImage.url)
                    .from(productImage)
                    .where(productImage.product.id.in(productIds)
                            .and(productImage.isThumbnail.isTrue()))
                    .fetch();

            imageMap = imageRows.stream()
                    .collect(Collectors.toMap(
                            row -> row.get(productImage.product.id),
                            row -> row.get(productImage.url),
                            (existing, replacement) -> existing // 중복 키가 있을 때 기존 값 유지
                    ));
        } else {
            imageMap = new HashMap<>();
        }

        // 4. DTO 변환
        List<ProductListDTO> dtoList = productRows.stream()
                .map(row -> {
                    Long id = row.get(product.id);
                    String name = row.get(product.name);
                    Integer price = row.get(product.price);
                    String status = row.get(product.status).name();
                    Boolean active = row.get(product.active);
                    String sellerName = row.get(seller.name);
                    String categoryName = row.get(category.name);
                    String imageUrl = imageMap.get(id);

                    return ProductListDTO.builder()
                            .id(id)
                            .name(name)
                            .price(price)
                            .status(status)
                            .isActive(active)
                            .imageThumbnailUrl(imageUrl)
                            .sellerName(sellerName)
                            .categoryName(categoryName)
                            .build();
                })
                .toList();

        // 5. 전체 개수 조회
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
