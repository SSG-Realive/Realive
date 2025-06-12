package com.realive.serviceimpl.seller;

import com.realive.dto.logs.PayoutLogDTO;
import com.realive.repository.logs.PayoutLogRepository;
import com.realive.service.seller.SellerPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerPayoutServiceImpl implements SellerPayoutService {

    private final PayoutLogRepository payoutLogRepository;

    @Override
    public List<PayoutLogDTO> getPayoutLogsBySellerId(Long sellerId) {
        // sellerId는 Long이지만 Repository는 Integer로 받으므로 변환
        return payoutLogRepository.findBySellerId(sellerId.intValue()).stream()
                .map(PayoutLogDTO::fromEntity)
                .toList();
    }
}