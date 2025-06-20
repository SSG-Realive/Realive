package com.realive.dto.auction;

import com.realive.domain.common.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuctionPaymentRequestDTO {
    @NotNull
    private final Integer auctionId;
    
    @NotNull
    private final String paymentKey; // 토스페이먼츠 결제 키
    
    @NotNull
    private final String tossOrderId; // 토스페이먼츠 주문 ID
    
    @NotNull
    private final PaymentType paymentMethod;
    
    @NotNull
    private final String receiverName;
    
    @NotNull
    private final String phone;
    
    @NotNull
    private final String deliveryAddress;
} 