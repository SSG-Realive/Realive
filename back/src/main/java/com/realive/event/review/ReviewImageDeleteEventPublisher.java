package com.realive.event.review;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewImageDeleteEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(List<String> imageUrlsToDelete) {
        if (imageUrlsToDelete != null && !imageUrlsToDelete.isEmpty()) {
            publisher.publishEvent(new ReviewImageDeleteEvent(imageUrlsToDelete));
        }
    }
}