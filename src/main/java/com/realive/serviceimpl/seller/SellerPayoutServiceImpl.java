package com.realive.serviceimpl.seller;

import com.realive.dto.logs.PayoutLogDTO;
import com.realive.domain.logs.PayoutLog;
import com.realive.repository.logs.PayoutLogRepository;
import com.realive.repository.order.OrderItemRepository;
import com.realive.service.seller.SellerPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerPayoutServiceImpl implements SellerPayoutService {

    private final PayoutLogRepository payoutLogRepository;
    private final OrderItemRepository orderItemRepository;

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

    @Override
    public void generatePayoutLogIfNotExists(Long orderId) {
         List<Long> sellerIds = orderItemRepository.findSellerIdsByOrderId(orderId);
        if (sellerIds == null || sellerIds.isEmpty()) return;

        LocalDate now = LocalDate.now();
        LocalDate periodStart = now.with(DayOfWeek.MONDAY);
        LocalDate periodEnd = now.with(DayOfWeek.SUNDAY);

        LocalDateTime startDateTime = periodStart.atStartOfDay();
        LocalDateTime endDateTime = periodEnd.plusDays(1).atStartOfDay(); // 다음 주 월요일 0시

        for (Long sellerId : sellerIds) {
            Integer intSellerId = sellerId.intValue();

            // 이미 정산 로그가 있다면 스킵
            boolean exists = payoutLogRepository.existsBySellerIdAndPeriodStartAndPeriodEnd(
                    intSellerId, periodStart, periodEnd);
            if (exists) continue;

            // 매출 집계
            Integer totalSales = orderItemRepository.sumDeliveredSalesBySellerAndPeriod(
                    sellerId, startDateTime, endDateTime);

            if (totalSales == null || totalSales == 0) continue;

            int commission = (int) (totalSales * 0.1); // 10% 수수료
            int payout = totalSales - commission;

            PayoutLog log = new PayoutLog();
            log.setSellerId(intSellerId);
            log.setPeriodStart(periodStart);
            log.setPeriodEnd(periodEnd);
            log.setTotalSales(totalSales);
            log.setTotalCommission(commission);
            log.setPayoutAmount(payout);
            log.setProcessedAt(LocalDateTime.now());

            payoutLogRepository.save(log);
        }
        
    }

    
}