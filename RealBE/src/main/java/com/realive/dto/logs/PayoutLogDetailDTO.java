package com.realive.dto.logs;

import com.realive.domain.logs.PayoutLog;
import com.realive.domain.order.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PayoutLogDetailDTO {
    private final PayoutLogDTO payoutInfo;
    private final List<SalesWithCommissionDTO> salesDetails;

    /**
     * PayoutLog 엔티티와 OrderItem 리스트로부터 PayoutLogDetailDTO를 생성합니다.
     */
    public static PayoutLogDetailDTO from(PayoutLog payoutLog, List<OrderItem> orderItems) {
        // PayoutLog를 PayoutLogDTO로 변환
        PayoutLogDTO payoutInfo = PayoutLogDTO.fromEntity(payoutLog);

        // OrderItem 리스트를 SalesWithCommissionDTO 리스트로 변환
        List<SalesWithCommissionDTO> salesDetails = orderItems.stream()
                .map(item -> {
                    // SalesLogDTO 생성
                    SalesLogDTO salesLog = SalesLogDTO.builder()
                            .id(item.getId().intValue())
                            .orderItemId(item.getId().intValue())
                            .productId(item.getProduct().getId().intValue())
                            .sellerId(item.getProduct().getSeller().getId().intValue())
                            .customerId(item.getOrder().getCustomer().getId()) // Long 타입
                            .quantity(item.getQuantity())
                            .unitPrice(item.getPrice()) // OrderItem의 price 필드 사용
                            .totalPrice(item.getPrice() * item.getQuantity()) // 수량 * 단가 계산
                            .soldAt(item.getOrder().getOrderedAt().toLocalDate()) // orderedAt 사용
                            .build();

                    // CommissionLogDTO 생성 (10% 수수료 기준)
                    CommissionLogDTO commissionLog = CommissionLogDTO.builder()
                            .id(item.getId().intValue())
                            .salesLogId(item.getId().intValue())
                            .commissionRate(java.math.BigDecimal.valueOf(0.1)) // BigDecimal로 변환
                            .commissionAmount((int)((item.getPrice() * item.getQuantity()) * 0.1)) // 총액의 10%
                            .recordedAt(item.getOrder().getOrderedAt()) // LocalDateTime 타입
                            .build();

                    return SalesWithCommissionDTO.builder()
                            .salesLog(salesLog)
                            .commissionLog(commissionLog)
                            .build();
                })
                .collect(Collectors.toList());

        return PayoutLogDetailDTO.builder()
                .payoutInfo(payoutInfo)
                .salesDetails(salesDetails)
                .build();
    }


}