package com.realive.event.review;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewImageDeleteEventPublisher {

    private final ApplicationEventPublisher publisher;

    /**
     * 리뷰 이미지 파일 삭제 이벤트를 발행합니다.
     * @param imageUrlsToDelete 삭제할 이미지 URL 목록 (최종 URL)
     */
    public void publish(List<String> imageUrlsToDelete) {
        if (imageUrlsToDelete != null && !imageUrlsToDelete.isEmpty()) {
            publisher.publishEvent(new ReviewImageDeleteEvent(imageUrlsToDelete));
        }
    }
}