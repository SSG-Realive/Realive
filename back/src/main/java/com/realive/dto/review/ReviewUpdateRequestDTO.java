package com.realive.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewUpdateRequestDTO {

    @NotNull
    private Long reviewId;
    @Min(value = 1)
    @Max(value = 5)
    private int score;
    private String comment;
    private String imageUrl;
}
