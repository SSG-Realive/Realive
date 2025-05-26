package com.realive.dto.product;

import com.realive.domain.common.enums.DeliveryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품 배송 정책 정보를 전달하는 DTO
 * - 상품 등록/조회 시 함께 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPolicyDTO {

    private DeliveryType type;      // 배송 유형: 무료배송, 유료배송
    private int cost;               // 배송비 (원 단위, 0이면 무료)
    private String regionLimit;     // 지역 제한 정보 (예: "서울/경기만 가능")
}