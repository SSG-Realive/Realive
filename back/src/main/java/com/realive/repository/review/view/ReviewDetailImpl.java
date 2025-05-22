package com.realive.repository.review.view;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.review.QSellerReview;
import com.realive.domain.review.QSellerReviewImage;
import com.realive.domain.seller.QSeller;
import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ReviewDetailImpl implements ReviewDetail {

    private final JPAQueryFactory queryFactory;

    //리뷰 상세 조회
    @Override
    public Optional<ReviewResponseDTO> findReviewDetailById(Long id) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QSeller seller = QSeller.seller;

        ReviewResponseDTO dto = queryFactory
                .select(Projections.constructor(ReviewResponseDTO.class,
                        sellerReview.id,
                        sellerReview.rating,
                        sellerReview.content,
                        sellerReview.createdAt,
                        sellerReviewImage.imageUrl,
                        seller.name,
                        seller.id))
                .from(sellerReview)
                .leftJoin(sellerReviewImage).on(sellerReviewImage.review.eq(sellerReview))
                .leftJoin(sellerReview.seller, seller)
                .where(sellerReview.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(dto);
    }

    //판매자에 대한 리뷰 리스트 조회
    @Override
    public Page<ReviewListResponseDTO> findSellerReviewsBySellerId(Long sellerId, Pageable pageable) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QSeller seller = QSeller.seller;

        List<ReviewListResponseDTO> content = queryFactory
                .select(Projections.constructor(ReviewListResponseDTO.class,
                        sellerReview.id,
                        sellerReview.rating,
                        sellerReview.content,
                        sellerReview.createdAt,
                        seller.name))
                .from(sellerReview)
                .leftJoin(sellerReviewImage).on(sellerReviewImage.review.eq(sellerReview))
                .leftJoin(sellerReview.seller, seller)
                .where(sellerReview.seller.id.eq(sellerId))
                .orderBy(sellerReview.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(sellerReview)
                .where(sellerReview.seller.id.eq(sellerId))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    //내가 작성한 리뷰 리스트 조회
    @Override
    public Page<ReviewListResponseDTO> findSellerReviewsByMe(Long customerId, Pageable pageable) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QSeller seller = QSeller.seller;

        List<ReviewListResponseDTO> content = queryFactory
                .select(Projections.constructor(ReviewListResponseDTO.class,
                        sellerReview.id,
                        sellerReview.rating,
                        sellerReview.content,
                        sellerReview.createdAt,
                        seller.name))
                .from(sellerReview)
                .leftJoin(sellerReviewImage).on(sellerReviewImage.review.eq(sellerReview))
                .leftJoin(sellerReview.seller, seller)
                .where(sellerReview.customer.id.eq(customerId))
                .orderBy(sellerReview.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(sellerReview)
                .where(sellerReview.customer.id.eq(customerId))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }
}