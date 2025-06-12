package com.realive.service.seller;

import com.realive.dto.logs.PayoutLogDTO;

import java.time.LocalDate;
import java.util.List;

public interface SellerPayoutService {
    List<PayoutLogDTO> getPayoutLogsBySellerId(Long sellerId);

    List<PayoutLogDTO> getPayoutLogsByDate(Long sellerId, LocalDate date);
}
