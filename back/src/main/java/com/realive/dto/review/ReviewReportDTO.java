package com.realive.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 리뷰 신고 요청 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReportDTO {

    private Integer sellerReviewId;

    @NotNull
    private Integer reporterId;

    @NotBlank
    private String reason;

}
