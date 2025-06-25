package com.realive.serviceimpl.seller;

import com.realive.domain.common.enums.OrderStatus;
import com.realive.dto.seller.SellerDashboardResponseDTO;
import com.realive.dto.seller.SellerSalesStatsDTO;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.seller.SellerQnaRepository;
import com.realive.repository.logs.SalesLogRepository;
import com.realive.repository.logs.CommissionLogRepository;
import com.realive.repository.review.SellerReviewRepository;
import com.realive.service.seller.SellerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerDashboardServiceImpl implements SellerDashboardService {

    private final ProductRepository productRepository;
    private final SellerQnaRepository sellerQnaRepository;
    private final OrderRepository orderRepository;
    private final SalesLogRepository salesLogRepository;
    private final CommissionLogRepository commissionLogRepository;
    private final SellerReviewRepository sellerReviewRepository;

    @Override
    public SellerDashboardResponseDTO getDashboardInfo(Long sellerId) {
        // 오늘 날짜의 시작과 끝
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // 진행 중 주문 상태 정의
        List<OrderStatus> inProgressStatuses = List.of(
                OrderStatus.PAYMENT_COMPLETED,
                OrderStatus.DELIVERY_PREPARING,
                OrderStatus.DELIVERY_IN_PROGRESS
        );

        // 기본 통계
        long totalProductCount = productRepository.countBySellerIdAndActiveTrue(sellerId);
        long unansweredQnaCount = sellerQnaRepository.countBySellerIdAndIsAnsweredFalseAndIsActiveTrue(sellerId);
        long todayProductCount = productRepository.countTodayProductBySellerId(sellerId, startOfDay, endOfDay);
        long totalQnaCount = sellerQnaRepository.countBySellerIdAndIsActiveTrue(sellerId);
        long inProgressOrderCount = orderRepository.countInProgressOrders(sellerId, inProgressStatuses);

        // 추가 통계
        Long totalCustomers = salesLogRepository.countDistinctCustomersBySellerId(sellerId);
        Double averageRating = sellerReviewRepository.getAverageRatingBySellerId(sellerId);
        Long totalReviews = sellerReviewRepository.countReviewsBySellerId(sellerId);

        // 판매 통계 (최근 30일)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        SellerSalesStatsDTO salesStats = getSalesStatistics(sellerId, startDate, endDate);

        return SellerDashboardResponseDTO.builder()
                .totalProductCount(totalProductCount)
                .unansweredQnaCount(unansweredQnaCount)
                .todayProductCount(todayProductCount)
                .totalQnaCount(totalQnaCount)
                .inProgressOrderCount(inProgressOrderCount)
                .salesStats(salesStats)
                .totalCustomers(totalCustomers != null ? totalCustomers : 0L)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .build();
    }

    @Override
    public SellerSalesStatsDTO getSalesStatistics(Long sellerId, LocalDate startDate, LocalDate endDate) {
        Long totalOrders = salesLogRepository.countDistinctOrdersBySellerIdAndSoldAtBetween(sellerId.intValue(), startDate, endDate);
        Number totalRevenueNum = salesLogRepository.sumTotalPriceBySellerIdAndSoldAtBetween(sellerId.intValue(), startDate, endDate);
        Number totalFeesNum = commissionLogRepository.sumCommissionAmountBySellerAndDateRange(sellerId.intValue(), startDate, endDate);

        List<SellerSalesStatsDTO.DailySalesDTO> dailySalesTrend = getDailySalesTrend(sellerId, startDate, endDate);
        List<SellerSalesStatsDTO.MonthlySalesDTO> monthlySalesTrend = getMonthlySalesTrend(sellerId, startDate, endDate);

        return SellerSalesStatsDTO.builder()
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .totalRevenue(totalRevenueNum != null ? totalRevenueNum.doubleValue() : 0.0)
                .totalFees(totalFeesNum != null ? totalFeesNum.doubleValue() : 0.0)
                .dailySalesTrend(dailySalesTrend)
                .monthlySalesTrend(monthlySalesTrend)
                .build();
    }

    @Override
    public List<SellerSalesStatsDTO.DailySalesDTO> getDailySalesTrend(Long sellerId, LocalDate startDate, LocalDate endDate) {
        return salesLogRepository.getDailySalesBySellerId(sellerId, startDate, endDate)
                .stream()
                .map(row -> {
                    LocalDate date = (LocalDate) row[0];
                    Number orderCountNum = (Number) row[1];
                    Number revenueNum = (Number) row[2];

                    return SellerSalesStatsDTO.DailySalesDTO.builder()
                            .date(date)
                            .orderCount(orderCountNum != null ? orderCountNum.longValue() : 0L)
                            .revenue(revenueNum != null ? revenueNum.doubleValue() : 0.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SellerSalesStatsDTO.MonthlySalesDTO> getMonthlySalesTrend(Long sellerId, LocalDate startDate, LocalDate endDate) {
        return salesLogRepository.getMonthlySalesBySellerId(sellerId, startDate, endDate)
                .stream()
                .map(row -> {
                    String yearMonth = (String) row[0];
                    Number orderCountNum = (Number) row[1];
                    Number revenueNum = (Number) row[2];

                    return SellerSalesStatsDTO.MonthlySalesDTO.builder()
                            .yearMonth(yearMonth)
                            .orderCount(orderCountNum != null ? orderCountNum.longValue() : 0L)
                            .revenue(revenueNum != null ? revenueNum.doubleValue() : 0.0)
                            .build();
                })
                .collect(Collectors.toList());
    }
}