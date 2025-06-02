package com.realive.event.review;

import com.realive.domain.review.SellerReview;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ReviewImageUploadEvent {

    private final SellerReview sellerReview; // DB 커밋된 리뷰 엔티티
    private final List<String> tempImageUrls; // 클라이언트가 보낸 임시 이미지 URL 리스트
}