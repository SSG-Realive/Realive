package com.realive.event.review;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ReviewImageDeleteEvent {
    private final List<String> imageUrlsToDelete;
}