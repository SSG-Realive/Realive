package com.realive.dto.admin.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 판매자 랭킹 조회용 DTO
 */
@Getter
@Builder
@AllArgsConstructor  // 모든 필드를 파라미터로 받는 생성자 생성
public class SellerRankingDTO {

    private Long sellerId;
    private String sellerName;
    private Double avgRating;
    private Long reviewCount;
  
}
    

