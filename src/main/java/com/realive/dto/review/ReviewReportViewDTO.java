package com.realive.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 신고된 리뷰 조회
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewReportViewDTO{
    
    private Integer reportId;

    private Integer sellerReviewId;

    @NotNull
    private Integer reporterId;

    @NotBlank
    private String reason;

    private String originalReviewContent;

}
