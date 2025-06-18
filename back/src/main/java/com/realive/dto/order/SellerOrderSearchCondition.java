package com.realive.dto.order;

import java.time.LocalDateTime;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.dto.page.PageRequestDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)

public class SellerOrderSearchCondition extends PageRequestDTO{
    
    private String productName;
    private DeliveryStatus deliveryStatus;
    private LocalDateTime fromDate; //필터 시작일
    private LocalDateTime toDate;   //필터 종료일
}
