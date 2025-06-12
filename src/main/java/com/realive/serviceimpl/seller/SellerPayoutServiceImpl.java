package com.realive.serviceimpl.seller;

import com.realive.dto.logs.PayoutLogDTO;
import com.realive.domain.logs.PayoutLog;
import com.realive.repository.logs.PayoutLogRepository;
import com.realive.service.seller.SellerPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerPayoutServiceImpl implements SellerPayoutService {

    private final PayoutLogRepository payoutLogRepository;

    /**
     * 판매자 ID로 전체 정산 로그를 조회합니다.
     *
     * @param sellerId 판매자 ID
     * @return 정산 로그 DTO 목록
     */
    @Override
    public List<PayoutLogDTO> getPayoutLogsBySellerId(Long sellerId) {
        Integer intSellerId = sellerId.intValue();

        return payoutLogRepository.findBySellerId(intSellerId).stream()
                .map(PayoutLogDTO::fromEntity)
                .toList();
    }

    /**
     * 특정 날짜가 정산 기간(periodStart ~ periodEnd)에 포함된 정산 로그를 조회합니다.
     * (단, 로그인한 판매자의 데이터만 필터링)
     *
     * @param sellerId 판매자 ID
     * @param date 기준 날짜
     * @return 필터링된 정산 로그 DTO 목록
     */
    @Override
    public List<PayoutLogDTO> getPayoutLogsByDate(Long sellerId, LocalDate date) {
        Integer intSellerId = sellerId.intValue();

        return payoutLogRepository.findByDateInPeriod(date).stream()
                .filter(p -> p.getSellerId() != null && p.getSellerId().equals(sellerId.intValue()))
                .map(PayoutLogDTO::fromEntity)
                .toList();
    }
}