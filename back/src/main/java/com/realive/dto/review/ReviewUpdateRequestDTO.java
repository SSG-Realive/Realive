package com.realive.dto.review;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewUpdateRequestDTO {

    @Min(value = 1)
    @Max(value = 5)
    private double rating;
    private String comment;
    @Nullable
    private String imageUrl;
}
