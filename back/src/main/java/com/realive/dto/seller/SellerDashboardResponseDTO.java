package com.realive.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 판매자 대시보드 응답 DTO
 * - 등록된 상품 수
 * - 미답변 QnA 수
 * - 오늘 등록된 상품 수
 * - 총 QnA 개수
 * - 진행 중인 주문 수
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerDashboardResponseDTO {

    // 등록된 상품 수
    private long totalProductCount;

    // 미답변 QnA 수
    private long unansweredQnaCount;

    // 오늘 등록된 상품 수
    private long todayProductCount;

    // 총 QnA 개수
    private long totalQnaCount;

    // 진행 중인 주문 수
    private long inProgressOrderCount;
}