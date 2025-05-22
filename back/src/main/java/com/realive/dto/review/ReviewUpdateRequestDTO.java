package com.realive.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReviewUpdateRequestDTO {

    private Long customerId;

    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private Double rating;

    private String content; // 내용 변경이 없을수도 있으니 @NotBlank 삭제

    private List<String> imageUrls;
}