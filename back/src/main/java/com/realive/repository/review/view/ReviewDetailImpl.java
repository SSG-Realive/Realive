package com.realive.repository.review.view;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.review.QSellerReview;
import com.realive.domain.review.QSellerReviewImage;
import com.realive.domain.customer.QCustomer;
import com.realive.domain.order.QOrder;
import com.realive.domain.order.QOrderItem; // QOrderItem 필요
import com.realive.domain.product.QProduct; // QProduct 필요
import com.realive.domain.seller.QSeller;
import com.realive.dto.review.MyReviewResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReviewDetailImpl implements ReviewDetail {

    private final JPAQueryFactory queryFactory;

    // 리뷰 상세 조회
    @Override
    public Optional<ReviewResponseDTO> findReviewDetailById(Long id) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem; // OrderItem 엔티티 Q클래스
        QProduct product = QProduct.product;     // Product 엔티티 Q클래스
        QSeller seller = QSeller.seller;
        QCustomer customer = QCustomer.customer; // Customer 엔티티 Q클래스

        Tuple reviewTuple = queryFactory
                .select(
                        sellerReview.id,
                        sellerReview.order.id,
                        sellerReview.customer.id,
                        sellerReview.seller.id,
                        // ⭐ productName 가져오기: orderItem을 서브쿼리로 조인하여 가져옵니다.
                        // 이 방식은 Order 엔티티에 orderItems 컬렉션이 없어도 동작합니다.
                        ExpressionUtils.as(
                                JPAExpressions.select(product.name) // OrderItem의 product.name 선택
                                        .from(orderItem) // OrderItem 테이블에서
                                        .join(orderItem.product, product) // OrderItem과 Product 조인
                                        .where(orderItem.order.id.eq(sellerReview.order.id)) // 해당 리뷰의 주문 ID와 일치하는 OrderItem
                                        .limit(1), // 여러 OrderItem 중 첫 번째 (대표) 상품 이름
                                "productName"
                        ),
                        sellerReview.rating,
                        sellerReview.content,
                        sellerReview.createdAt,
                        sellerReview.isHidden
                )
                .from(sellerReview)
                .leftJoin(sellerReview.order, order) // SellerReview는 Order를 ManyToOne으로 참조
                .leftJoin(sellerReview.customer, customer) // SellerReview는 Customer를 ManyToOne으로 참조
                .leftJoin(sellerReview.seller, seller) // SellerReview는 Seller를 ManyToOne으로 참조
                .where(sellerReview.id.eq(id))
                .fetchOne();

        if (reviewTuple == null) {
            return Optional.empty();
        }

        // Tuple에서 직접 DTO로 매핑
        ReviewResponseDTO reviewDto = ReviewResponseDTO.builder()
                .reviewId(reviewTuple.get(sellerReview.id))
                .orderId(reviewTuple.get(sellerReview.order.id))
                .customerId(reviewTuple.get(sellerReview.customer.id))
                .sellerId(reviewTuple.get(sellerReview.seller.id))
                .productName(reviewTuple.get(ExpressionUtils.as(JPAExpressions.select(product.name).from(orderItem).join(orderItem.product, product).where(orderItem.order.id.eq(sellerReview.order.id)).limit(1), "productName"))) // alias 사용
                .rating(reviewTuple.get(sellerReview.rating))
                .content(reviewTuple.get(sellerReview.content))
                .createdAt(reviewTuple.get(sellerReview.createdAt))
                .isHidden(reviewTuple.get(sellerReview.isHidden))
                .build();

        // 이미지 URL은 별도로 조회하여 N+1 방지
        List<String> imageUrls = queryFactory
                .select(sellerReviewImage.imageUrl)
                .from(sellerReviewImage)
                .where(sellerReviewImage.review.id.eq(id))
                .fetch();

        reviewDto.setImageUrls(imageUrls);

        return Optional.of(reviewDto);
    }

    // 판매자에 대한 리뷰 리스트 조회
    @Override
    public Page<ReviewResponseDTO> findSellerReviewsBySellerId(Long sellerId, Pageable pageable) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProduct product = QProduct.product;
        QSeller seller = QSeller.seller;
        QCustomer customer = QCustomer.customer;

        List<Tuple> reviewTuples = queryFactory
                .select(
                        sellerReview.id,
                        sellerReview.order.id,
                        sellerReview.customer.id,
                        sellerReview.seller.id,
                        ExpressionUtils.as(
                                JPAExpressions.select(product.name)
                                        .from(orderItem)
                                        .join(orderItem.product, product)
                                        .where(orderItem.order.id.eq(sellerReview.order.id))
                                        .limit(1),
                                "productName"
                        ),
                        sellerReview.rating,
                        sellerReview.content,
                        sellerReview.createdAt,
                        sellerReview.isHidden
                )
                .from(sellerReview)
                .leftJoin(sellerReview.order, order)
                .leftJoin(sellerReview.customer, customer)
                .leftJoin(sellerReview.seller, seller)
                .where(sellerReview.seller.id.eq(sellerId))
                .orderBy(sellerReview.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<ReviewResponseDTO> content = reviewTuples.stream().map(tuple ->
                ReviewResponseDTO.builder()
                        .reviewId(tuple.get(sellerReview.id))
                        .orderId(tuple.get(sellerReview.order.id))
                        .customerId(tuple.get(sellerReview.customer.id))
                        .sellerId(tuple.get(sellerReview.seller.id))
                        .productName(tuple.get(ExpressionUtils.as(JPAExpressions.select(product.name).from(orderItem).join(orderItem.product, product).where(orderItem.order.id.eq(sellerReview.order.id)).limit(1), "productName")))
                        .rating(tuple.get(sellerReview.rating))
                        .content(tuple.get(sellerReview.content))
                        .createdAt(tuple.get(sellerReview.createdAt))
                        .isHidden(tuple.get(sellerReview.isHidden))
                        .build()
        ).collect(Collectors.toList());

        // 이미지 URL 별도 조회 및 매핑 (이전과 동일)
        List<Long> reviewIds = content.stream().map(ReviewResponseDTO::getReviewId).collect(Collectors.toList());
        Map<Long, List<String>> reviewImageUrlsMap = queryFactory
                .select(sellerReviewImage.review.id, sellerReviewImage.imageUrl)
                .from(sellerReviewImage)
                .where(sellerReviewImage.review.id.in(reviewIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(sellerReviewImage.review.id),
                        Collectors.mapping(tuple -> tuple.get(sellerReviewImage.imageUrl), Collectors.toList())
                ));

        content.forEach(review -> review.setImageUrls(reviewImageUrlsMap.getOrDefault(review.getReviewId(), List.of())));

        long total = queryFactory
                .selectFrom(sellerReview)
                .where(sellerReview.seller.id.eq(sellerId))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    // 내가 작성한 리뷰 리스트 조회
    @Override
    public Page<MyReviewResponseDTO> findMyReviewsByCustomerId(Long customerId, Pageable pageable) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProduct product = QProduct.product;
        QCustomer customer = QCustomer.customer;

        List<Tuple> reviewTuples = queryFactory
                .select(
                        sellerReview.id,
                        sellerReview.order.id,
                        ExpressionUtils.as(
                                JPAExpressions.select(product.name)
                                        .from(orderItem)
                                        .join(orderItem.product, product)
                                        .where(orderItem.order.id.eq(sellerReview.order.id))
                                        .limit(1),
                                "productName"
                        ),
                        sellerReview.rating,
                        sellerReview.content,
                        sellerReview.createdAt
                )
                .from(sellerReview)
                .leftJoin(sellerReview.order, order)
                .leftJoin(sellerReview.customer, customer)
                .where(sellerReview.customer.id.eq(customerId))
                .orderBy(sellerReview.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<MyReviewResponseDTO> content = reviewTuples.stream().map(tuple ->
                MyReviewResponseDTO.builder()
                        .reviewId(tuple.get(sellerReview.id))
                        .orderId(tuple.get(sellerReview.order.id))
                        .productName(tuple.get(ExpressionUtils.as(JPAExpressions.select(product.name).from(orderItem).join(orderItem.product, product).where(orderItem.order.id.eq(sellerReview.order.id)).limit(1), "productName")))
                        .rating(tuple.get(sellerReview.rating))
                        .content(tuple.get(sellerReview.content))
                        .createdAt(tuple.get(sellerReview.createdAt))
                        .build()
        ).collect(Collectors.toList());

        // 이미지 URL 별도 조회 및 매핑 (이전과 동일)
        List<Long> reviewIds = content.stream().map(MyReviewResponseDTO::getReviewId).collect(Collectors.toList());
        Map<Long, List<String>> reviewImageUrlsMap = queryFactory
                .select(sellerReviewImage.review.id, sellerReviewImage.imageUrl)
                .from(sellerReviewImage)
                .where(sellerReviewImage.review.id.in(reviewIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(sellerReviewImage.review.id),
                        Collectors.mapping(tuple -> tuple.get(sellerReviewImage.imageUrl), Collectors.toList())
                ));

        content.forEach(review -> review.setImageUrls(reviewImageUrlsMap.getOrDefault(review.getReviewId(), List.of())));

        long total = queryFactory
                .selectFrom(sellerReview)
                .where(sellerReview.customer.id.eq(customerId))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }
}