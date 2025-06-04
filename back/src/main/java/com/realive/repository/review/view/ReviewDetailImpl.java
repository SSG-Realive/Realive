package com.realive.repository.review.view;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.review.QSellerReview;
import com.realive.domain.review.QSellerReviewImage;
import com.realive.domain.customer.QCustomer;
import com.realive.domain.order.QOrder;
import com.realive.domain.order.QOrderItem;
import com.realive.domain.product.QProduct;
import com.realive.domain.seller.QSeller;
import com.realive.dto.review.MyReviewResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository; // 이 어노테이션을 추가하여 Spring 빈으로 등록

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository // 이제 ReviewDetailImpl이 직접 Spring 빈으로 등록됩니다.
@RequiredArgsConstructor
public class ReviewDetailImpl { // ReviewDetail 인터페이스 구현 제거

    private final JPAQueryFactory queryFactory;

    // 특정 리뷰의 상세 정보 조회
    public Optional<ReviewResponseDTO> findReviewDetailById(Long id) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProduct product = QProduct.product;
        QSeller seller = QSeller.seller;
        QCustomer customer = QCustomer.customer;

        // 리뷰 정보와 관련 데이터(주문, 고객, 판매자 등) 조회
        Tuple reviewTuple = queryFactory
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
                .where(sellerReview.id.eq(id))
                .fetchOne();

        // 리뷰가 없으면 빈 Optional 반환
        if (reviewTuple == null) {
            return Optional.empty();
        }

        // 조회된 데이터를 DTO로 변환
        ReviewResponseDTO reviewDto = ReviewResponseDTO.builder()
                .reviewId(reviewTuple.get(sellerReview.id))
                .orderId(reviewTuple.get(sellerReview.order.id))
                .customerId(reviewTuple.get(sellerReview.customer.id))
                .sellerId(reviewTuple.get(sellerReview.seller.id))
                .productName(reviewTuple.get(ExpressionUtils.as(JPAExpressions.select(product.name).from(orderItem).join(orderItem.product, product).where(orderItem.order.id.eq(sellerReview.order.id)).limit(1), "productName")))
                .rating(reviewTuple.get(sellerReview.rating))
                .content(reviewTuple.get(sellerReview.content))
                .createdAt(reviewTuple.get(sellerReview.createdAt))
                .isHidden(reviewTuple.get(sellerReview.isHidden))
                .build();

        // 리뷰에 연결된 이미지 URL 조회
        List<String> imageUrls = queryFactory
                .select(sellerReviewImage.imageUrl)
                .from(sellerReviewImage)
                .where(sellerReviewImage.review.id.eq(id))
                .fetch();

        // DTO에 이미지 URL 추가
        reviewDto.setImageUrls(imageUrls);

        // 완성된 DTO 반환
        return Optional.of(reviewDto);
    }

    // 판매자에 대한 리뷰 목록을 페이지네이션으로 조회
    public Page<ReviewResponseDTO> findSellerReviewsBySellerId(Long sellerId, Pageable pageable) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProduct product = QProduct.product;
        QSeller seller = QSeller.seller;
        QCustomer customer = QCustomer.customer;

        // 판매자의 리뷰 목록 조회 (최신순 정렬)
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

        // 조회 결과를 DTO 리스트로 변환
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

        // 리뷰 ID로 이미지 URL 조회
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

        // 각 리뷰에 이미지 URL 추가
        content.forEach(review -> review.setImageUrls(reviewImageUrlsMap.getOrDefault(review.getReviewId(), List.of())));

        // 판매자의 총 리뷰 수 계산
        long total = queryFactory
                .selectFrom(sellerReview)
                .where(sellerReview.seller.id.eq(sellerId))
                .fetchCount();

        // 페이지 객체로 반환
        return new PageImpl<>(content, pageable, total);
    }

    // 고객이 작성한 리뷰 목록을 페이지네이션으로 조회
    public Page<MyReviewResponseDTO> findMyReviewsByCustomerId(Long customerId, Pageable pageable) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProduct product = QProduct.product;
        QCustomer customer = QCustomer.customer;

        // 고객의 리뷰 목록 조회 (최신순 정렬)
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

        // 조회 결과를 DTO 리스트로 변환
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

        // 리뷰 ID로 이미지 URL 조회
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

        // 각 리뷰에 이미지 URL 추가
        content.forEach(review -> review.setImageUrls(reviewImageUrlsMap.getOrDefault(review.getReviewId(), List.of())));

        // 고객의 총 리뷰 수 계산
        long total = queryFactory
                .selectFrom(sellerReview)
                .where(sellerReview.customer.id.eq(customerId))
                .fetchCount();

        // 페이지 객체로 반환
        return new PageImpl<>(content, pageable, total);
    }
}