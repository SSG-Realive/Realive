package com.realive.dto.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.dto.page.PageRequestDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)

public class SellerOrderSearchCondition extends PageRequestDTO{
    
    private String productName;
    private DeliveryStatus deliveryStatus;
    private LocalDateTime fromDate; //필터 시작일
    private LocalDateTime toDate;   //필터 종료일
}
