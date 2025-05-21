package com.realive.dto.review;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewListResponseDTO {
    private List<ReviewResponseDTO> reviews;
    private int totalCount;
    private int page;
    private int size;
}