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
public class ReviewDetailImpl implements ReviewDetail{

    private final JPAQueryFactory queryFactory;

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
                .fetchOne(); // 단일 결과 조회

        return Optional.ofNullable(dto);
    }

    @Override
    public Page<ReviewListResponseDTO> findSellerReviewsBySellerId(Long sellerId, Pageable pageable) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QSeller seller = QSeller.seller;

        // 콘텐츠 조회
        List<ReviewListResponseDTO> content = queryFactory
                .select(Projections.constructor(ReviewListResponseDTO.class,
                        sellerReview.id,
                        sellerReview.rating,
                        sellerReview.content,
                        sellerReview.createdAt,
                        seller.name)) // ReviewListResponseDTO에 맞는 필드 선택
                .from(sellerReview)
                .leftJoin(sellerReviewImage).on(sellerReviewImage.review.eq(sellerReview))
                .leftJoin(sellerReview.seller, seller)
                .where(sellerReview.seller.id.eq(sellerId))
                .orderBy(sellerReview.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회
        long total = queryFactory
                .selectFrom(sellerReview)
                .where(sellerReview.seller.id.eq(sellerId))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ReviewListResponseDTO> findSellerReviewsByMe(Pageable pageable) {
        QSellerReview sellerReview = QSellerReview.sellerReview;
        QSellerReviewImage sellerReviewImage = QSellerReviewImage.sellerReviewImage;
        QSeller seller = QSeller.seller;

        // 콘텐츠 조회
        List<ReviewListResponseDTO> content = queryFactory
                .select(Projections.constructor(ReviewListResponseDTO.class,
                        sellerReview.id,
                        sellerReview.rating,
                        sellerReview.content,
                        sellerReview.createdAt,
                        seller.name)) // ReviewListResponseDTO에 맞는 필드 선택
                .from(sellerReview)
                .leftJoin(sellerReviewImage).on(sellerReviewImage.review.eq(sellerReview))
                .leftJoin(sellerReview.seller, seller)
                .where(sellerReview.seller.id.eq(sellerId))
                .orderBy(sellerReview.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회
        long total = queryFactory
                .selectFrom(sellerReview)
                .where(sellerReview.seller.id.eq(sellerId))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

}
