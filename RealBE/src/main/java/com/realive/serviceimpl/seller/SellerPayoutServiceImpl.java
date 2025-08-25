package com.realive.serviceimpl.seller;

import com.realive.domain.logs.CommissionLog;
import com.realive.domain.logs.SalesLog;
import com.realive.domain.order.OrderItem;
import com.realive.dto.logs.CommissionLogDTO;
import com.realive.dto.logs.PayoutLogDTO;
import com.realive.domain.logs.PayoutLog;
import com.realive.dto.logs.PayoutLogDetailDTO;
import com.realive.dto.logs.SalesLogDTO;
import com.realive.dto.seller.SellerPayoutSummaryDTO;
import com.realive.repository.logs.CommissionLogRepository;
import com.realive.repository.logs.PayoutLogRepository;
import com.realive.repository.logs.SalesLogRepository;
import com.realive.repository.order.OrderItemRepository;
import com.realive.service.seller.SellerPayoutService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
    private final SalesLogRepository salesLogRepository;
    private final CommissionLogRepository commissionLogRepository;

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

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : orderItems) {
            // 1. SalesLog 생성 및 저장된 객체 받기
            SalesLog savedSalesLog = createSalesLog(item);

            // 2. CommissionLog 생성 (주석 제거)
            createCommissionLog(savedSalesLog.getId());
        }
         List<Long> sellerIds = orderItemRepository.findSellerIdsByOrderId(orderId);
        if (sellerIds == null || sellerIds.isEmpty()) return;

        LocalDate now = LocalDate.now();
        LocalDate periodStart = now.with(DayOfWeek.MONDAY);
        LocalDate periodEnd = now.with(DayOfWeek.SUNDAY);

        LocalDateTime startDateTime = periodStart.atStartOfDay();
        LocalDateTime endDateTime = periodEnd.plusDays(1).atStartOfDay(); // 다음 주 월요일 0시

        for (Long sellerId : sellerIds) {
            Integer intSellerId = sellerId.intValue();

            // 매출 집계
            Integer totalSales = orderItemRepository.sumDeliveredSalesBySellerAndPeriod(
                    sellerId, startDateTime, endDateTime);

            if (totalSales == null || totalSales == 0) continue;

            int commission = (int) (totalSales * 0.1); // 10% 수수료
            int payout = totalSales - commission;

            // 기존 PayoutLog 찾기
            PayoutLog existingLog = payoutLogRepository.findBySellerIdAndPeriodStartAndPeriodEnd(
                    intSellerId, periodStart, periodEnd).orElse(null);
            
            if (existingLog != null) {
                // 기존 로그 업데이트
                existingLog.setTotalSales(totalSales);
                existingLog.setTotalCommission(commission);
                existingLog.setPayoutAmount(payout);
                existingLog.setProcessedAt(LocalDateTime.now()); // 처리 시간 갱신
                payoutLogRepository.save(existingLog);
            } else {
                // 새 로그 생성
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

    @Override
    public List<PayoutLogDTO> getPayoutLogsByPeriod(Long sellerId, LocalDate from, LocalDate to) {
        Integer intSellerId = sellerId.intValue();
        return payoutLogRepository.findBySellerIdAndPeriodRange(intSellerId, from, to)
                .stream()
                .map(PayoutLogDTO::fromEntity)
                .toList();
    }

    @Override
    public SellerPayoutSummaryDTO getPayoutSummary(Long sellerId, LocalDate from, LocalDate to) {
        Integer intSellerId = sellerId.intValue();
        // ✅ 데이터베이스에서 직접 필터링된 데이터 가져오기
        List<PayoutLog> logs = payoutLogRepository.findBySellerIdAndPeriodRange(intSellerId, from, to);

        int totalPayoutAmount = logs.stream().mapToInt(l -> l.getPayoutAmount() != null ? l.getPayoutAmount() : 0).sum();
        int totalCommission = logs.stream().mapToInt(l -> l.getTotalCommission() != null ? l.getTotalCommission() : 0).sum();
        int totalSales = logs.stream().mapToInt(l -> l.getTotalSales() != null ? l.getTotalSales() : 0).sum();
        int payoutCount = logs.size();

        return SellerPayoutSummaryDTO.builder()
                .totalPayoutAmount(totalPayoutAmount)
                .totalCommission(totalCommission)
                .totalSales(totalSales)
                .payoutCount(payoutCount)
                .build();
    }

    @Override
    public PayoutLogDetailDTO getPayoutLogDetail(Long sellerId, Integer payoutLogId) {
        PayoutLog log = payoutLogRepository.findById(payoutLogId)
                .orElseThrow(() -> new EntityNotFoundException("PayoutLog not found"));

        if (!log.getSellerId().equals(sellerId.intValue())) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        // 예시: 해당 기간의 판매건 리스트 조회
        List<OrderItem> items = orderItemRepository.findDeliveredBySellerAndPeriod(
                sellerId, log.getPeriodStart().atStartOfDay(), log.getPeriodEnd().plusDays(1).atStartOfDay()
        );

        return PayoutLogDetailDTO.from(log, items);
    }


    @Override
    public SalesLog createSalesLog(OrderItem orderItem) {  // void → SalesLog 변경
        SalesLog salesLog = new SalesLog();
        salesLog.setOrderItemId(orderItem.getId().intValue());
        salesLog.setProductId(orderItem.getProduct().getId().intValue());
        salesLog.setSellerId(orderItem.getProduct().getSeller().getId().intValue());
        salesLog.setCustomerId(orderItem.getOrder().getCustomer().getId());
        salesLog.setTotalPrice(orderItem.getPrice() * orderItem.getQuantity());
        salesLog.setQuantity(orderItem.getQuantity());
        salesLog.setSoldAt(LocalDate.now());
        salesLog.setUnitPrice(orderItem.getPrice());

        return salesLogRepository.save(salesLog);  // return 추가
    }

    @Override
    public void createCommissionLog(Integer salesLogId) {
        SalesLog salesLog = salesLogRepository.findById(salesLogId)
                .orElseThrow(() -> new EntityNotFoundException("SalesLog not found"));

        CommissionLog commissionLog = new CommissionLog();
        commissionLog.setSalesLogId(salesLogId);
        commissionLog.setCommissionRate(java.math.BigDecimal.valueOf(0.1)); // 10%
        commissionLog.setCommissionAmount((int)(salesLog.getTotalPrice() * 0.1));
        commissionLog.setRecordedAt(java.time.LocalDateTime.now());

        commissionLogRepository.save(commissionLog);
    }

    @Override
    public List<SalesLogDTO> getSalesLogsBySeller(Long sellerId, LocalDate from, LocalDate to) {
        return salesLogRepository.findBySoldAtBetween(from, to)  // ← 기존 메서드 사용
                .stream()
                .filter(salesLog -> salesLog.getSellerId().equals(sellerId.intValue()))  // ← 필터링 추가
                .map(SalesLogDTO::fromEntity)
                .toList();
    }

    @Override
    public List<CommissionLogDTO> getCommissionLogsBySeller(Long sellerId, LocalDate from, LocalDate to) {
        List<Integer> salesLogIds = salesLogRepository.findBySoldAtBetween(from, to)  // ← 기존 메서드 사용
                .stream()
                .filter(salesLog -> salesLog.getSellerId().equals(sellerId.intValue()))  // ← 필터링 추가
                .map(SalesLog::getId)
                .toList();

        return commissionLogRepository.findBySalesLogIdIn(salesLogIds)
                .stream()
                .map(CommissionLogDTO::fromEntity)
                .toList();
    }

    @Override
    public Page<PayoutLogDTO> getPayoutLogsBySellerId(Long sellerId, Pageable pageable) {
        Integer intSellerId = sellerId.intValue();
        return payoutLogRepository.findBySellerId(intSellerId, pageable)
                .map(PayoutLogDTO::fromEntity);
    }
}

