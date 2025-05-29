package com.realive.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReviewCreateRequestDTO {

    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    @NotNull(message = "판매자 ID는 필수입니다.")
    private Long sellerId;

    private Long customerId;

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private Double rating;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private List<String> imageUrls;

    public interface CreateValidation {}
}