package com.realive.service.seller;

import com.realive.dto.logs.PayoutLogDTO;

import java.util.List;

public interface SellerPayoutService {
    List<PayoutLogDTO> getPayoutLogsBySellerId(Long sellerId);
}
