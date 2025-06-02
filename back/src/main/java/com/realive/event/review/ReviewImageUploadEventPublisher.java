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

    /**
     * 리뷰 이미지 업로드 (임시 -> 최종) 확정 이벤트를 발행합니다.
     * @param sellerReview 리뷰 엔티티 (트랜잭션 커밋 후 핸들러에서 사용)
     * @param tempImageUrls 클라이언트가 업로드하여 받은 임시 이미지 URL 목록
     */
    public void publish(SellerReview sellerReview, List<String> tempImageUrls) {
        publisher.publishEvent(new ReviewImageUploadEvent(sellerReview, tempImageUrls));
    }
}