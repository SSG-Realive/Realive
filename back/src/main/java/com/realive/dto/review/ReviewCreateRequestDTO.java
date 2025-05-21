package com.realive.dto.review;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewCreateRequestDTO {

    @NotNull
    private Long orderId;
    @Min(value = 1)
    @Max(value = 5)
    @NotNull
    private double rating;
    @NotBlank
    private String comment;
    @Nullable
    private String imageUrl;
    private LocalDateTime createdAt;

}
