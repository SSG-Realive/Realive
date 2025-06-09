package com.realive.event.review;

import com.realive.domain.review.SellerReview;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewImageUploadEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(SellerReview sellerReview, List<String> tempImageUrls) {
        publisher.publishEvent(new ReviewImageUploadEvent(sellerReview, tempImageUrls));
    }
}