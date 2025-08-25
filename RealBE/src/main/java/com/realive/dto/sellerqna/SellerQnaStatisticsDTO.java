package com.realive.dto.sellerqna;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerQnaStatisticsDTO {
    private long totalCount;      // 전체 문의 수
    private long unansweredCount; // 미답변 수
    private long answeredCount;   // 답변완료 수
    private double answerRate;    // 답변률 (%)
}