package com.realive.event.review;

import com.realive.domain.review.SellerReview;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ReviewImageUploadEvent {

    private final SellerReview sellerReview;
    private final List<String> tempImageUrls;
}