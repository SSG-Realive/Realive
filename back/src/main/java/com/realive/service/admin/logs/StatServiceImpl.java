package com.realive.service.admin.logs;

import com.realive.domain.logs.SalesLog;
import com.realive.dto.logs.SalesLogDTO;
import com.realive.dto.logs.salessum.DailySalesSummaryDTO;
import com.realive.dto.logs.salessum.MonthlySalesLogDetailListDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.dto.logs.salessum.SalesLogDetailListDTO;
import com.realive.repository.logs.CommissionLogRepository;
import com.realive.repository.logs.PayoutLogRepository;
import com.realive.repository.logs.PenaltyLogRepository;
import com.realive.repository.logs.SalesLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {

    private final SalesLogRepository salesLogRepository;
    private final CommissionLogRepository commissionLogRepository;
    private final PayoutLogRepository payoutLogRepository;
    private final PenaltyLogRepository penaltyLogRepository;

    @Override
    public DailySalesSummaryDTO getDailySalesSummary(LocalDate date) {
        Integer totalSalesAmount = salesLogRepository.sumTotalPriceByDate(date);
        Integer totalSalesCount = salesLogRepository.countBySoldAt(date);
        Integer totalQuantity = salesLogRepository.sumQuantityByDate(date);
        Integer totalCommissionAmount = commissionLogRepository.sumCommissionAmountByDate(date);

        return DailySalesSummaryDTO.builder()
                .date(date)
                .totalSalesCount(totalSalesCount != null ? totalSalesCount : 0)
                .totalSalesAmount(totalSalesAmount != null ? totalSalesAmount : 0) // DTO가 Integer
                .totalQuantity(totalQuantity != null ? totalQuantity : 0)
                // DailySalesSummaryDTO에 totalCommission 필드가 있다면 추가
                // .totalCommission(totalCommissionAmount != null ? totalCommissionAmount : 0)
                .build();
    }

    @Override
    public SalesLogDetailListDTO getDailySalesLogDetails(LocalDate date) {
        List<SalesLog> salesLogs = salesLogRepository.findBySoldAtBetween(date, date);
        List<SalesLogDTO> salesLogDTOs = salesLogs.stream()
                .map(this::convertToSalesLogDTO)
                .collect(Collectors.toList());

        return SalesLogDetailListDTO.builder()
                .date(date)
                .salesLogs(salesLogDTOs)
                .build();
    }

    @Override
    public MonthlySalesSummaryDTO getMonthlySalesSummary(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Integer totalSalesAmount = salesLogRepository.sumTotalPriceBySoldAtBetween(startDate, endDate);
        Long totalSalesCountLong = salesLogRepository.countDistinctOrdersBySoldAtBetween(startDate, endDate);

        List<SalesLog> monthlySales = salesLogRepository.findBySoldAtBetween(startDate, endDate);
        Integer totalQuantity = monthlySales.stream()
                .mapToInt(sl -> sl.getQuantity() != null ? sl.getQuantity() : 0)
                .sum();

        return MonthlySalesSummaryDTO.builder()
                .month(yearMonth) // DTO가 YearMonth
                .totalSalesCount(totalSalesCountLong != null ? totalSalesCountLong.intValue() : 0) // DTO가 Integer
                .totalSalesAmount(totalSalesAmount != null ? totalSalesAmount : 0) // DTO가 Integer
                .totalQuantity(totalQuantity) // DTO가 Integer
                .build();
    }

    @Override
    public MonthlySalesLogDetailListDTO getMonthlySalesLogDetails(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<SalesLog> monthlySales = salesLogRepository.findBySoldAtBetween(startDate, endDate);
        List<SalesLogDTO> salesLogDTOs = monthlySales.stream()
                .map(this::convertToSalesLogDTO)
                .collect(Collectors.toList());

        return MonthlySalesLogDetailListDTO.builder()
                .month(yearMonth) // DTO가 YearMonth
                .salesLogs(salesLogDTOs)
                .build();
    }

    @Override
    public List<DailySalesSummaryDTO> getDailySummariesInMonth(YearMonth yearMonth) {
        List<DailySalesSummaryDTO> result = new ArrayList<>();
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            result.add(getDailySalesSummary(currentDate));
            currentDate = currentDate.plusDays(1);
        }
        return result;
    }

    @Override
    public DailySalesSummaryDTO getSellerDailySalesSummary(Integer sellerId, LocalDate date) {
        Integer totalSalesAmount = salesLogRepository.sumTotalPriceBySellerIdAndSoldAtBetween(sellerId, date, date);
        Long distinctOrderCountLong = salesLogRepository.countDistinctOrdersBySellerIdAndSoldAtBetween(sellerId, date, date);
        Integer commissionForSellerOnDate = commissionLogRepository.sumCommissionAmountBySellerAndDateRange(sellerId, date, date);

        List<SalesLog> sellerSalesOnDate = salesLogRepository.findBySoldAtBetween(date, date).stream()
                .filter(sl -> sl.getSellerId() != null && sl.getSellerId().equals(sellerId))
                .collect(Collectors.toList());
        Integer totalQuantity = sellerSalesOnDate.stream().mapToInt(sl -> sl.getQuantity() != null ? sl.getQuantity() : 0).sum();

        return DailySalesSummaryDTO.builder()
                .date(date)
                .totalSalesCount(distinctOrderCountLong != null ? distinctOrderCountLong.intValue() : 0)
                .totalSalesAmount(totalSalesAmount != null ? totalSalesAmount : 0)
                .totalQuantity(totalQuantity)
                // .totalCommission(commissionForSellerOnDate != null ? commissionForSellerOnDate : 0) // DailySalesSummaryDTO에 필드 추가 필요
                .build();
    }

    @Override
    public MonthlySalesSummaryDTO getSellerMonthlySalesSummary(Integer sellerId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Integer totalSalesAmount = salesLogRepository.sumTotalPriceBySellerIdAndSoldAtBetween(sellerId, startDate, endDate);
        Long distinctOrderCountLong = salesLogRepository.countDistinctOrdersBySellerIdAndSoldAtBetween(sellerId, startDate, endDate);
        Integer commissionForSellerInMonth = commissionLogRepository.sumCommissionAmountBySellerAndDateRange(sellerId, startDate, endDate);

        List<SalesLog> sellerMonthlySales = salesLogRepository.findBySoldAtBetween(startDate, endDate).stream()
                .filter(sl -> sl.getSellerId() != null && sl.getSellerId().equals(sellerId))
                .collect(Collectors.toList());
        Integer totalQuantity = sellerMonthlySales.stream().mapToInt(sl -> sl.getQuantity() != null ? sl.getQuantity() : 0).sum();

        return MonthlySalesSummaryDTO.builder()
                .month(yearMonth)
                .totalSalesCount(distinctOrderCountLong != null ? distinctOrderCountLong.intValue() : 0)
                .totalSalesAmount(totalSalesAmount != null ? totalSalesAmount : 0)
                .totalQuantity(totalQuantity)
                // .totalCommission(commissionForSellerInMonth != null ? commissionForSellerInMonth : 0) // MonthlySalesSummaryDTO에 필드 추가 필요
                .build();
    }

    @Override
    public DailySalesSummaryDTO getProductDailySalesSummary(Integer productId, LocalDate date) {
        Integer totalSalesAmount = salesLogRepository.sumTotalPriceByProductIdAndSoldAtBetween(productId, date, date);
        Integer totalQuantity = salesLogRepository.sumQuantityByProductIdAndSoldAtBetween(productId, date, date);

        List<SalesLog> productSalesOnDate = salesLogRepository.findBySoldAtBetween(date, date).stream()
                .filter(sl -> sl.getProductId() != null && sl.getProductId().equals(productId))
                .collect(Collectors.toList());
        long distinctOrderCount = productSalesOnDate.stream().map(SalesLog::getOrderItemId).distinct().count();

        return DailySalesSummaryDTO.builder()
                .date(date)
                .totalSalesCount((int) distinctOrderCount)
                .totalSalesAmount(totalSalesAmount != null ? totalSalesAmount : 0)
                .totalQuantity(totalQuantity != null ? totalQuantity : 0)
                .build();
    }

    @Override
    public MonthlySalesSummaryDTO getProductMonthlySalesSummary(Integer productId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Integer totalSalesAmount = salesLogRepository.sumTotalPriceByProductIdAndSoldAtBetween(productId, startDate, endDate);
        Integer totalQuantity = salesLogRepository.sumQuantityByProductIdAndSoldAtBetween(productId, startDate, endDate);

        List<SalesLog> productMonthlySales = salesLogRepository.findBySoldAtBetween(startDate, endDate).stream()
                .filter(sl -> sl.getProductId() != null && sl.getProductId().equals(productId))
                .collect(Collectors.toList());
        long distinctOrderCount = productMonthlySales.stream().map(SalesLog::getOrderItemId).distinct().count();

        return MonthlySalesSummaryDTO.builder()
                .month(yearMonth)
                .totalSalesCount((int) distinctOrderCount)
                .totalSalesAmount(totalSalesAmount != null ? totalSalesAmount : 0)
                .totalQuantity(totalQuantity != null ? totalQuantity : 0)
                .build();
    }

    @Override
    public Map<String, Object> getDashboardStats(LocalDate date) {
        Map<String, Object> dashboardStats = new HashMap<>();
        DailySalesSummaryDTO dailySalesSummary = getDailySalesSummary(date);
        dashboardStats.put("dailySalesSummary", dailySalesSummary);
        YearMonth currentMonth = YearMonth.from(date);
        dashboardStats.put("monthlySalesSummary", getMonthlySalesSummary(currentMonth));

        List<DailySalesSummaryDTO> last7DaysStats = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate pastDate = date.minusDays(i);
            last7DaysStats.add(getDailySalesSummary(pastDate));
        }
        dashboardStats.put("last7DaysStats", last7DaysStats);

        Integer totalPayoutToday = payoutLogRepository.sumPayoutAmountByProcessedDate(date);
        dashboardStats.put("totalPayoutToday", totalPayoutToday != null ? BigDecimal.valueOf(totalPayoutToday) : BigDecimal.ZERO);

        Integer penaltyCountToday = penaltyLogRepository.countByCreatedAtDate(date);
        dashboardStats.put("penaltyCountToday", penaltyCountToday != null ? penaltyCountToday : 0);

        return dashboardStats;
    }

    private SalesLogDTO convertToSalesLogDTO(SalesLog salesLog) {
        if (salesLog == null) return null;
        return SalesLogDTO.builder()
                .id(salesLog.getId())
                .orderItemId(salesLog.getOrderItemId())
                .productId(salesLog.getProductId())
                .sellerId(salesLog.getSellerId())
                .customerId(salesLog.getCustomerId())
                .quantity(salesLog.getQuantity())
                .unitPrice(salesLog.getUnitPrice())
                .totalPrice(salesLog.getTotalPrice())
                .soldAt(salesLog.getSoldAt())
                .build();
    }
}
