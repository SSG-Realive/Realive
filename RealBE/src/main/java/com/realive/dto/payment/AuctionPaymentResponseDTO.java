package com.realive.dto.payment;

import com.realive.domain.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionPaymentResponseDTO {
    private final Long id;
    private final Integer auctionId;
    private final Integer amount;
    private final PaymentStatus status;     // PAID / FAILED / CANCELED ...
    private final String receiverName;
    private final String phone;
    private final String deliveryAddress;
    private final LocalDateTime paidAt;     // null이면 미결제
}