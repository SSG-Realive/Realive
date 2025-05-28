// src/main/java/com/realive/service/admin/logs/StatServiceImpl.java
package com.realive.service.admin.logs;

// --- 기존 import 문들 ---
import com.realive.domain.logs.CommissionLog;
import com.realive.domain.logs.PayoutLog;
import com.realive.domain.logs.PenaltyLog;
import com.realive.domain.logs.SalesLog;
import com.realive.domain.seller.Seller;
import com.realive.dto.logs.AdminDashboardDTO;
import com.realive.dto.logs.CommissionLogDTO;
import com.realive.dto.logs.PayoutLogDTO;
import com.realive.dto.logs.PenaltyLogDTO;
import com.realive.dto.logs.ProductLogDTO;
import com.realive.dto.logs.SalesLogDTO;
import com.realive.dto.logs.SalesWithCommissionDTO;
import com.realive.dto.logs.salessum.CategorySalesSummaryDTO;
import com.realive.dto.logs.salessum.DailySalesSummaryDTO;
import com.realive.dto.logs.salessum.MonthlySalesLogDetailListDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.dto.logs.salessum.SalesLogDetailListDTO;
import com.realive.repository.admin.approval.ApprovalRepository;
import com.realive.repository.logs.CommissionLogRepository;
import com.realive.repository.logs.PayoutLogRepository;
import com.realive.repository.logs.PenaltyLogRepository;
import com.realive.repository.logs.SalesLogRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.seller.SellerRepository;
// import com.realive.repository.user.UserRepository;

// --- DTO stats 패키지 import ---
// 아래 패키지에 MemberSummaryStatsDTO, MemberPeriodStatsDTO, DateBasedValueDTO, MonthBasedValueDTO 등이 있다고 가정
import com.realive.dto.logs.stats.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {

    private final ApprovalRepository approvalRepository;
    private final SalesLogRepository salesLogRepository;
    private final PenaltyLogRepository penaltyLogRepository;
    private final ProductRepository productRepository;
    private final PayoutLogRepository payoutLogRepository;
    private final CommissionLogRepository commissionLogRepository;
    private final SellerRepository sellerRepository;
    // private final UserRepository userRepository;

    @Override
    public AdminDashboardDTO getAdminDashboard(LocalDate date, String periodType) {
        log.info("getAdminDashboard 호출됨 - 날짜: {}, 기간타입: {}", date, periodType);

        LocalDate startDate = date;
        LocalDate endDate = date;
        if ("MONTHLY".equalsIgnoreCase(periodType)) {
            startDate = date.withDayOfMonth(1);
            endDate = date.withDayOfMonth(date.lengthOfMonth());
        } else if (!"DAILY".equalsIgnoreCase(periodType)) {
            log.warn("지원하지 않는 periodType: {}. DAILY로 처리합니다.", periodType);
            periodType = "DAILY";
        }
        log.debug("조회 기간 설정: {} ~ {}", startDate, endDate);

        int pendingSellerCount = 0;
        if (approvalRepository != null) {
            List<Seller> pendingSellers = approvalRepository.findByIsApprovedFalseAndApprovedAtIsNull();
            pendingSellerCount = pendingSellers.size();
        }
        log.debug("승인 대기 판매자 수: {}", pendingSellerCount);

        List<SalesWithCommissionDTO> salesWithCommissionsMock = new ArrayList<>();
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(3, 8); i++) {
            SalesLogDTO salesLogMock = SalesLogDTO.builder().id(i + 1).orderItemId(i + 101).productId(ThreadLocalRandom.current().nextInt(1, 20))
                    .sellerId(ThreadLocalRandom.current().nextInt(1, 10)).customerId(ThreadLocalRandom.current().nextInt(1, 50))
                    .quantity(ThreadLocalRandom.current().nextInt(1, 5)).unitPrice(ThreadLocalRandom.current().nextInt(10000, 50000))
                    .totalPrice(ThreadLocalRandom.current().nextInt(10000, 250000)).soldAt(date.minusDays(ThreadLocalRandom.current().nextInt(0,3)))
                    .build();
            CommissionLogDTO commissionLogMock = CommissionLogDTO.builder().id(i + 1).salesLogId(salesLogMock.getId())
                    .commissionRate(new BigDecimal(String.valueOf(ThreadLocalRandom.current().nextDouble(0.05, 0.15))))
                    .commissionAmount((int) (salesLogMock.getTotalPrice() * ThreadLocalRandom.current().nextDouble(0.05, 0.15)))
                    .recordedAt(date.atTime(LocalTime.now().minusHours(i))).build();
            salesWithCommissionsMock.add(SalesWithCommissionDTO.builder().salesLog(salesLogMock).commissionLog(commissionLogMock).build());
        }
        List<PayoutLogDTO> payoutLogsMock = new ArrayList<>();
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(1, 4); i++) {
            payoutLogsMock.add(PayoutLogDTO.builder().id(i + 1).sellerId(ThreadLocalRandom.current().nextInt(1, 10))
                    .periodStart(date.minusDays(7+i)).periodEnd(date.minusDays(i))
                    .totalSales(ThreadLocalRandom.current().nextInt(1000000, 5000000))
                    .totalCommission(ThreadLocalRandom.current().nextInt(100000, 500000))
                    .payoutAmount(ThreadLocalRandom.current().nextInt(900000, 4500000))
                    .processedAt(date.atTime(LocalTime.NOON).minusDays(i)).build());
        }
        ProductLogDTO productLog = ProductLogDTO.builder()
                .salesWithCommissions(salesWithCommissionsMock)
                .payoutLogs(payoutLogsMock)
                .build();

        List<PenaltyLogDTO> penaltyLogsMock = new ArrayList<>();
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(0, 3); i++) {
            penaltyLogsMock.add(PenaltyLogDTO.builder().id(i + 1).customerId(ThreadLocalRandom.current().nextInt(1, 50))
                    .reason("Mock Penalty Reason " + (i+1)).points(ThreadLocalRandom.current().nextInt(10, 50))
                    .description("Mock penalty description.").createdAt(date.atTime(LocalTime.now().minusHours(i*2))).build());
        }

        MemberSummaryStatsDTO memberSummaryStats = MemberSummaryStatsDTO.builder()
                .totalMembers(10000L + ThreadLocalRandom.current().nextLong(1000)) // TODO: 실제 UserRepository.count()
                .newMembersInPeriod(ThreadLocalRandom.current().nextLong(
                        "DAILY".equalsIgnoreCase(periodType) ? 10 : 200,
                        "DAILY".equalsIgnoreCase(periodType) ? 30 : 500
                )) // TODO: 실제 기간 내 신규 가입자 수
                .uniqueVisitorsInPeriod(ThreadLocalRandom.current().nextLong( // 고유 방문자 수 Mock
                        "DAILY".equalsIgnoreCase(periodType) ? 500 : 5000,
                        "DAILY".equalsIgnoreCase(periodType) ? 2000 : 20000
                )) // TODO: 실제 '고유 방문자 수' (쿠키/IP/로그인 사용자 기반)
                .engagedUsersInPeriod(ThreadLocalRandom.current().nextLong( // 참여 사용자 수 Mock
                        "DAILY".equalsIgnoreCase(periodType) ? 100 : 1000,
                        "DAILY".equalsIgnoreCase(periodType) ? 500 : 5000
                ))   // TODO: 실제 '참여 사용자 수' (상품조회, 로그인 시도 등 의미있는 행동 기준)
                .activeUsersInPeriod(ThreadLocalRandom.current().nextLong( // 이 값에 대한 추이만 MemberPeriodStatsDTO에 포함
                        "DAILY".equalsIgnoreCase(periodType) ? 80 : 800,
                        "DAILY".equalsIgnoreCase(periodType) ? 400 : 4000
                ))    // TODO: 실제 '활동 사용자 수' (unique나 engaged와 다른 별도 정의 또는 둘 중 하나를 대표)
                .build();

        SalesSummaryStatsDTO salesSummaryStats = SalesSummaryStatsDTO.builder()
                .totalOrdersInPeriod(ThreadLocalRandom.current().nextLong("DAILY".equalsIgnoreCase(periodType) ? 20 : 400, "DAILY".equalsIgnoreCase(periodType) ? 80 : 1000))
                .totalRevenueInPeriod(ThreadLocalRandom.current().nextDouble("DAILY".equalsIgnoreCase(periodType) ? 1000000 : 20000000, "DAILY".equalsIgnoreCase(periodType) ? 5000000 : 80000000))
                .totalFeesInPeriod(ThreadLocalRandom.current().nextDouble(100000, 1000000)).build();
        AuctionSummaryStatsDTO auctionSummaryStats = AuctionSummaryStatsDTO.builder()
                .totalAuctionsInPeriod(ThreadLocalRandom.current().nextLong(5, 20)).totalBidsInPeriod(ThreadLocalRandom.current().nextLong(50, 200))
                .averageBidsPerAuctionInPeriod(ThreadLocalRandom.current().nextDouble(3, 10)).successRate(ThreadLocalRandom.current().nextDouble(0.5, 0.85))
                .failureRate(ThreadLocalRandom.current().nextDouble(0.1, 0.3)).build();
        ReviewSummaryStatsDTO reviewSummaryStats = ReviewSummaryStatsDTO.builder()
                .totalReviewsInPeriod(ThreadLocalRandom.current().nextLong(20, 100)).newReviewsInPeriod(ThreadLocalRandom.current().nextLong(5, 30))
                .averageRatingInPeriod(ThreadLocalRandom.current().nextDouble(3.0, 4.9)).deletionRate(ThreadLocalRandom.current().nextDouble(0.0, 0.1)).build();

        return AdminDashboardDTO.builder()
                .queryDate(date)
                .periodType(periodType.toUpperCase())
                .pendingSellerCount(pendingSellerCount)
                .productLog(productLog)
                .penaltyLogs(penaltyLogsMock)
                .memberSummaryStats(memberSummaryStats)
                .salesSummaryStats(salesSummaryStats)
                .auctionSummaryStats(auctionSummaryStats)
                .reviewSummaryStats(reviewSummaryStats)
                .build();
    }

    @Override
    public SalesPeriodStatsDTO getSalesStatistics(LocalDate startDate, LocalDate endDate,
                                                  Optional<Integer> sellerId, Optional<String> sortBy) {
        // (이전 답변과 동일한 Mock 구현 유지)
        log.info("getSalesStatistics 호출됨 - 기간: {} ~ {}, 판매자ID: {}, 정렬: {}", startDate, endDate, sellerId, sortBy);
        SalesSummaryStatsDTO summary = SalesSummaryStatsDTO.builder()
                .totalOrdersInPeriod(ThreadLocalRandom.current().nextLong(100, 1000))
                .totalRevenueInPeriod(ThreadLocalRandom.current().nextDouble(10000000, 50000000))
                .totalFeesInPeriod(ThreadLocalRandom.current().nextDouble(1000000, 5000000))
                .build();
        List<ProductSalesDetailDTO> productDetails = new ArrayList<>();
        for(int i=0; i < ThreadLocalRandom.current().nextInt(5,15); i++) {
            productDetails.add(ProductSalesDetailDTO.builder()
                    .productId(i+1)
                    .productName("Mock 상품 " + (i+1) + (sellerId.map(id -> " (판매자 " + id + ")").orElse("")))
                    .quantitySold(ThreadLocalRandom.current().nextLong(10, 100))
                    .totalRevenue(ThreadLocalRandom.current().nextDouble(100000, 1000000))
                    .build());
        }
        sortBy.ifPresent(s -> {
            if (s.equalsIgnoreCase("revenue_desc")) {
                productDetails.sort(Comparator.comparing(ProductSalesDetailDTO::getTotalRevenue).reversed());
            }
        });
        List<SellerSalesDetailDTO> sellerDetails = new ArrayList<>();
        if (sellerId.isPresent()) {
            sellerDetails.add(SellerSalesDetailDTO.builder()
                    .sellerId(sellerId.get())
                    .sellerName("Mock 판매자 " + sellerId.get())
                    .quantitySold(ThreadLocalRandom.current().nextLong(50, 200))
                    .totalRevenue(ThreadLocalRandom.current().nextDouble(500000, 5000000))
                    .build());
        } else {
            for(int i=0; i < ThreadLocalRandom.current().nextInt(3,8); i++) {
                sellerDetails.add(SellerSalesDetailDTO.builder()
                        .sellerId(i+1)
                        .sellerName("Mock 판매자 " + (i+1))
                        .quantitySold(ThreadLocalRandom.current().nextLong(50, 200))
                        .totalRevenue(ThreadLocalRandom.current().nextDouble(500000, 5000000))
                        .build());
            }
        }
        List<DateBasedValueDTO<Double>> dailyRevenueTrend = generateDateBasedTrend(startDate, endDate,
                () -> ThreadLocalRandom.current().nextDouble(100000, 1000000));
        return SalesPeriodStatsDTO.builder()
                .summary(summary)
                .productSalesDetails(productDetails)
                .sellerSalesDetails(sellerDetails)
                .dailyRevenueTrend(dailyRevenueTrend)
                .build();
    }

    @Override
    public AuctionPeriodStatsDTO getAuctionPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        // (이전 답변과 동일한 Mock 구현 유지)
        log.info("getAuctionPeriodStatistics 호출됨 - 기간: {} ~ {}", startDate, endDate);
        long totalAuctions = ThreadLocalRandom.current().nextLong(50, 200);
        long successfulAuctions = (long) (totalAuctions * ThreadLocalRandom.current().nextDouble(0.6, 0.9));
        AuctionSummaryStatsDTO summary = AuctionSummaryStatsDTO.builder()
                .totalAuctionsInPeriod(totalAuctions).totalBidsInPeriod(ThreadLocalRandom.current().nextLong(500, 2000))
                .averageBidsPerAuctionInPeriod(totalAuctions > 0 ? ThreadLocalRandom.current().nextDouble(5, 20) : 0)
                .successRate(totalAuctions > 0 ? (double) successfulAuctions / totalAuctions : 0)
                .failureRate(totalAuctions > 0 ? (double) (totalAuctions - successfulAuctions) / totalAuctions : 0).build();
        List<DateBasedValueDTO<Long>> dailyAuctionTrend = generateDateBasedTrend(startDate, endDate, () -> ThreadLocalRandom.current().nextLong(1, 10));
        List<DateBasedValueDTO<Long>> dailyBidTrend = generateDateBasedTrend(startDate, endDate, () -> ThreadLocalRandom.current().nextLong(10, 50));
        return AuctionPeriodStatsDTO.builder()
                .summary(summary).averageParticipantsPerAuction(ThreadLocalRandom.current().nextLong(3, 10))
                .dailyAuctionCountTrend(dailyAuctionTrend).dailyBidCountTrend(dailyBidTrend).build();
    }

    @Override
    public MemberPeriodStatsDTO getMemberPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("getMemberPeriodStatistics 호출됨 - 기간: {} ~ {}", startDate, endDate);

        // MemberSummaryStatsDTO 생성 (3가지 활동 지표 Mock 데이터로 채움)
        MemberSummaryStatsDTO summary = MemberSummaryStatsDTO.builder()
                .totalMembers(15000L + ThreadLocalRandom.current().nextLong(2000)) // TODO: 실제 데이터 조회
                .newMembersInPeriod(ThreadLocalRandom.current().nextLong(100, 500)) // TODO: 실제 데이터 조회
                .uniqueVisitorsInPeriod(ThreadLocalRandom.current().nextLong(2000, 8000)) // TODO: 실제 '고유 방문자 수'
                .engagedUsersInPeriod(ThreadLocalRandom.current().nextLong(1000, 4000))  // TODO: 실제 '참여 사용자 수'
                .activeUsersInPeriod(ThreadLocalRandom.current().nextLong(1500, 6000))   // TODO: 실제 '활동 사용자 수' (정의 필요)
                .build();

        // MemberPeriodStatsDTO의 추이 데이터 필드 (사용자 확정 DTO 구조 반영)
        // dailyActiveUserTrend와 monthlyActiveUserTrend는 summary.activeUsersInPeriod 값의 추이를 나타냄
        List<DateBasedValueDTO<Long>> dailyNewUserTrend = generateDateBasedTrend(startDate, endDate,
                () -> ThreadLocalRandom.current().nextLong(5, 30));
        List<DateBasedValueDTO<Long>> dailyActiveUserTrend = generateDateBasedTrend(startDate, endDate,
                () -> { // summary.activeUsersInPeriod를 기반으로 일별 추이 Mock 생성
                    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    if (daysBetween <= 0) return summary.getActiveUsersInPeriod(); // 기간이 0일 이하일 경우 (방어 코드)
                    return summary.getActiveUsersInPeriod() / daysBetween + ThreadLocalRandom.current().nextLong(-20, 21); // 단순 평균 + 변동성
                });

        List<MonthBasedValueDTO<Long>> monthlyNewUserTrend = generateMonthBasedTrend(startDate, endDate,
                () -> ThreadLocalRandom.current().nextLong(100, 500));
        List<MonthBasedValueDTO<Long>> monthlyActiveUserTrend = generateMonthBasedTrend(startDate, endDate,
                () -> { // summary.activeUsersInPeriod를 기반으로 월별 추이 Mock 생성
                    long monthsBetween = ChronoUnit.MONTHS.between(YearMonth.from(startDate), YearMonth.from(endDate)) + 1;
                    if (monthsBetween <= 0) return summary.getActiveUsersInPeriod(); // 기간이 0개월 이하일 경우 (방어 코드)
                    return summary.getActiveUsersInPeriod() / monthsBetween + ThreadLocalRandom.current().nextLong(-200, 201); // 단순 평균 + 변동성
                });

        return MemberPeriodStatsDTO.builder()
                .summary(summary) // 모든 활동 지표가 포함된 summary
                .dailyNewUserTrend(dailyNewUserTrend)
                .dailyActiveUserTrend(dailyActiveUserTrend)   // 'activeUsersInPeriod'에 대한 추이
                .monthlyNewUserTrend(monthlyNewUserTrend)
                .monthlyActiveUserTrend(monthlyActiveUserTrend) // 'activeUsersInPeriod'에 대한 추이
                .build();
    }

    @Override
    public ReviewPeriodStatsDTO getReviewPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        // (이전 답변과 동일한 Mock 구현 유지)
        log.info("getReviewPeriodStatistics 호출됨 - 기간: {} ~ {}", startDate, endDate);
        long totalReviews = ThreadLocalRandom.current().nextLong(1000, 5000);
        long deletedReviews = (long) (totalReviews * ThreadLocalRandom.current().nextDouble(0.01, 0.05));
        ReviewSummaryStatsDTO summary = ReviewSummaryStatsDTO.builder()
                .totalReviewsInPeriod(totalReviews).newReviewsInPeriod(ThreadLocalRandom.current().nextLong(100, 500))
                .averageRatingInPeriod(ThreadLocalRandom.current().nextDouble(3.8, 4.9))
                .deletionRate(totalReviews > 0 ? (double) deletedReviews / totalReviews : 0).build();
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        ratingDistribution.put(1, ThreadLocalRandom.current().nextLong(10, 50));
        ratingDistribution.put(2, ThreadLocalRandom.current().nextLong(20, 80));
        ratingDistribution.put(3, ThreadLocalRandom.current().nextLong(100, 300));
        ratingDistribution.put(4, ThreadLocalRandom.current().nextLong(300, 800));
        ratingDistribution.put(5, ThreadLocalRandom.current().nextLong(500, 2000));
        List<DateBasedValueDTO<Long>> dailyReviewTrend = generateDateBasedTrend(startDate, endDate, () -> ThreadLocalRandom.current().nextLong(10, 50));
        return ReviewPeriodStatsDTO.builder()
                .summary(summary).ratingDistribution(ratingDistribution).dailyReviewCountTrend(dailyReviewTrend).build();
    }

    // === 기존 메소드들 (제공해주신 DTO 필드명에 맞춰 수정된 버전 유지) ===
    @Override
    public DailySalesSummaryDTO getDailySalesSummary(LocalDate date) {
        log.info("getDailySalesSummary 호출됨 - 날짜: {}", date);
        Integer salesCount = salesLogRepository.countBySoldAt(date);
        Integer salesAmount = salesLogRepository.sumTotalPriceByDate(date);
        Integer quantitySum = salesLogRepository.sumQuantityByDate(date);
        int totalSalesCount = (salesCount != null) ? salesCount : 0;
        int totalSalesAmount = (salesAmount != null) ? salesAmount : 0;
        int totalQuantity = (quantitySum != null) ? quantitySum : 0;
        return DailySalesSummaryDTO.builder().date(date).totalSalesCount(totalSalesCount)
                .totalSalesAmount(totalSalesAmount).totalQuantity(totalQuantity).build();
    }
    @Override
    public SalesLogDetailListDTO getDailySalesLogDetails(LocalDate date) {
        log.info("getDailySalesLogDetails 호출됨 - 날짜: {}", date);
        List<SalesLog> salesLogEntities = salesLogRepository.findBySoldAt(date);
        List<SalesLogDTO> salesLogDTOs = salesLogEntities.stream().map(SalesLogDTO::fromEntity).collect(Collectors.toList());
        return SalesLogDetailListDTO.builder().date(date).salesLogs(salesLogDTOs).build();
    }
    @Override
    public MonthlySalesSummaryDTO getMonthlySalesSummary(YearMonth yearMonth) {
        log.info("getMonthlySalesSummary 호출됨 - 연월: {}", yearMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        Integer salesAmount = salesLogRepository.sumTotalPriceBySoldAtBetween(startDate, endDate);
        Long orderCountFromRepo = salesLogRepository.countDistinctOrdersBySoldAtBetween(startDate, endDate);
        Integer quantitySum = salesLogRepository.sumQuantityBySoldAtBetween(startDate, endDate);
        int totalSalesAmountValue = (salesAmount != null) ? salesAmount : 0;
        int totalSalesCountValue = (orderCountFromRepo != null) ? orderCountFromRepo.intValue() : 0;
        int totalQuantityValue = (quantitySum != null) ? quantitySum : 0;
        return MonthlySalesSummaryDTO.builder().month(yearMonth).totalSalesCount(totalSalesCountValue)
                .totalSalesAmount(totalSalesAmountValue).totalQuantity(totalQuantityValue).build();
    }
    @Override
    public MonthlySalesLogDetailListDTO getMonthlySalesLogDetails(YearMonth yearMonth) {
        log.info("getMonthlySalesLogDetails 호출됨 - 연월: {}", yearMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<SalesLog> salesLogEntities = salesLogRepository.findBySoldAtBetween(startDate, endDate);
        List<SalesLogDTO> salesLogDTOs = salesLogEntities.stream().map(SalesLogDTO::fromEntity).collect(Collectors.toList());
        return MonthlySalesLogDetailListDTO.builder().month(yearMonth).salesLogs(salesLogDTOs).build();
    }
    @Override
    public List<DailySalesSummaryDTO> getDailySummariesInMonth(YearMonth yearMonth) {
        log.info("getDailySummariesInMonth 호출됨 (for 루프 사용) - 연월: {}", yearMonth);
        List<DailySalesSummaryDTO> dailySummaries = new ArrayList<>();
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = yearMonth.atDay(day);
            dailySummaries.add(getDailySalesSummary(currentDate));
        }
        return dailySummaries;
    }
    @Override
    public DailySalesSummaryDTO getSellerDailySalesSummary(Integer sellerId, LocalDate date) {
        log.info("getSellerDailySalesSummary 호출됨 - 판매자ID: {}, 날짜: {}", sellerId, date);
        Integer salesCount = salesLogRepository.countBySellerIdAndSoldAt(sellerId, date);
        Integer salesAmount = salesLogRepository.sumTotalPriceBySellerIdAndSoldAt(sellerId, date);
        Integer quantitySum = salesLogRepository.sumQuantityBySellerIdAndSoldAt(sellerId, date);
        int totalSalesCount = (salesCount != null) ? salesCount : 0;
        int totalSalesAmount = (salesAmount != null) ? salesAmount : 0;
        int totalQuantity = (quantitySum != null) ? quantitySum : 0;
        return DailySalesSummaryDTO.builder().date(date).totalSalesCount(totalSalesCount)
                .totalSalesAmount(totalSalesAmount).totalQuantity(totalQuantity).build();
    }
    @Override
    public MonthlySalesSummaryDTO getSellerMonthlySalesSummary(Integer sellerId, YearMonth yearMonth) {
        log.info("getSellerMonthlySalesSummary 호출됨 - 판매자ID: {}, 연월: {}", sellerId, yearMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        Integer salesAmount = salesLogRepository.sumTotalPriceBySellerIdAndSoldAtBetween(sellerId, startDate, endDate);
        Long orderCountFromRepo = salesLogRepository.countDistinctOrdersBySellerIdAndSoldAtBetween(sellerId, startDate, endDate);
        Integer quantitySum = salesLogRepository.sumQuantityBySellerIdAndSoldAtBetween(sellerId, startDate, endDate);
        int totalSalesAmountValue = (salesAmount != null) ? salesAmount : 0;
        int totalSalesCountValue = (orderCountFromRepo != null) ? orderCountFromRepo.intValue() : 0;
        int totalQuantityValue = (quantitySum != null) ? quantitySum : 0;
        return MonthlySalesSummaryDTO.builder().month(yearMonth).totalSalesCount(totalSalesCountValue)
                .totalSalesAmount(totalSalesAmountValue).totalQuantity(totalQuantityValue).build();
    }
    @Override
    public DailySalesSummaryDTO getProductDailySalesSummary(Integer productId, LocalDate date) {
        log.info("getProductDailySalesSummary 호출됨 - 상품ID: {}, 날짜: {}", productId, date);
        Integer salesAmount = salesLogRepository.sumTotalPriceByProductIdAndSoldAtBetween(productId, date, date);
        Integer quantitySum = salesLogRepository.sumQuantityByProductIdAndSoldAtBetween(productId, date, date);
        Integer salesCount = salesLogRepository.countByProductIdAndSoldAt(productId, date);
        int totalSalesAmount = (salesAmount != null) ? salesAmount : 0;
        int totalQuantity = (quantitySum != null) ? quantitySum : 0;
        int totalSalesCount = (salesCount != null) ? salesCount : 0;
        return DailySalesSummaryDTO.builder().date(date).totalSalesCount(totalSalesCount)
                .totalSalesAmount(totalSalesAmount).totalQuantity(totalQuantity).build();
    }
    @Override
    public MonthlySalesSummaryDTO getProductMonthlySalesSummary(Integer productId, YearMonth yearMonth) {
        log.info("getProductMonthlySalesSummary 호출됨 - 상품ID: {}, 연월: {}", productId, yearMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        Integer salesAmount = salesLogRepository.sumTotalPriceByProductIdAndSoldAtBetween(productId, startDate, endDate);
        Integer quantitySum = salesLogRepository.sumQuantityByProductIdAndSoldAtBetween(productId, startDate, endDate);
        Integer salesCount = salesLogRepository.countByProductIdAndSoldAtBetween(productId, startDate, endDate);
        int totalSalesAmountValue = (salesAmount != null) ? salesAmount : 0;
        int totalQuantityValue = (quantitySum != null) ? quantitySum : 0;
        int totalSalesCountValue = (salesCount != null) ? salesCount : 0;
        return MonthlySalesSummaryDTO.builder().month(yearMonth).totalSalesCount(totalSalesCountValue)
                .totalSalesAmount(totalSalesAmountValue).totalQuantity(totalQuantityValue).build();
    }
    @Override
    public Map<String, Object> getDashboardStats(LocalDate date) {
        log.info("관리자 대시보드 통합 통계 조회 (Map 반환) - 날짜: {}", date);
        Map<String, Object> dashboardData = new HashMap<>();
        int pendingSellerCount = 0;
        if (approvalRepository != null) {
            List<Seller> pendingSellers = approvalRepository.findByIsApprovedFalseAndApprovedAtIsNull();
            pendingSellerCount = pendingSellers.size();
        }
        long totalProductsCount = 0L;
        long newProductsTodayCount = 0L;
        if (productRepository != null) {
            totalProductsCount = productRepository.count();
            LocalDateTime startOfDayForProduct = date.atStartOfDay();
            LocalDateTime tomorrowStartOfDay = date.plusDays(1).atStartOfDay();
            newProductsTodayCount = productRepository.countByCreatedAtBetween(startOfDayForProduct, tomorrowStartOfDay);
        }
        List<SalesWithCommissionDTO> salesWithCommissionsData = new ArrayList<>();
        if (salesLogRepository != null && commissionLogRepository != null) {
            List<SalesLog> dailySalesLogs = salesLogRepository.findBySoldAt(date);
            for (SalesLog sale : dailySalesLogs) {
                SalesLogDTO salesLogDTO = SalesLogDTO.fromEntity(sale);
                Optional<CommissionLog> commissionOpt = commissionLogRepository.findBySalesLogId(sale.getId());
                CommissionLogDTO commissionLogDTO = commissionOpt.map(CommissionLogDTO::fromEntity).orElse(null);
                salesWithCommissionsData.add(SalesWithCommissionDTO.builder()
                        .salesLog(salesLogDTO)
                        .commissionLog(commissionLogDTO)
                        .build());
            }
        }
        List<PayoutLogDTO> payoutLogDataList = new ArrayList<>();
        if (payoutLogRepository != null) {
            LocalDateTime startOfDayForPayout = date.atStartOfDay();
            LocalDateTime endOfDayForPayout = date.atTime(LocalTime.MAX);
            List<PayoutLog> payoutEntities = payoutLogRepository.findByProcessedAtBetween(startOfDayForPayout, endOfDayForPayout);
            payoutLogDataList = payoutEntities.stream()
                    .map(PayoutLogDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        ProductLogDTO productLogData = ProductLogDTO.builder()
                .salesWithCommissions(salesWithCommissionsData)
                .payoutLogs(payoutLogDataList)
                .build();
        List<PenaltyLogDTO> penaltyLogDTOList = Collections.emptyList();
        if (penaltyLogRepository != null) {
            LocalDateTime startOfDayForPenalty = date.atStartOfDay();
            LocalDateTime endOfDayForPenalty = date.atTime(LocalTime.MAX);
            List<PenaltyLog> penaltyEntities = penaltyLogRepository.findByCreatedAtBetween(startOfDayForPenalty, endOfDayForPenalty);
            penaltyLogDTOList = penaltyEntities.stream()
                    .map(PenaltyLogDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        dashboardData.put("pendingSellerCount", pendingSellerCount);
        dashboardData.put("productLog", productLogData);
        dashboardData.put("penaltyLogs", penaltyLogDTOList);
        dashboardData.put("totalProducts", totalProductsCount);
        dashboardData.put("newProductsToday", newProductsTodayCount);
        DailySalesSummaryDTO todaySummary = getDailySalesSummary(date);
        dashboardData.put("todayTotalSalesAmount", todaySummary.getTotalSalesAmount());
        dashboardData.put("todayTotalSalesCount", todaySummary.getTotalSalesCount());
        log.info("대시보드 데이터(Map) 구성 완료: {}", dashboardData.keySet());
        return dashboardData;
    }
    @Override
    public List<CategorySalesSummaryDTO> getPlatformCategorySalesSummary(LocalDate startDate, LocalDate endDate) {
        log.info("플랫폼 전체 카테고리별 판매 요약 조회 요청 - 기간: {} ~ {}", startDate, endDate);
        if (salesLogRepository != null) {
            List<CategorySalesSummaryDTO> results = salesLogRepository.findCategorySalesSummaryBetween(startDate,endDate);
            return (results == null) ? Collections.emptyList() : results;
        } else {
            log.warn("SalesLogRepository가 주입되지 않았습니다. 카테고리별 판매 요약에 대해 빈 리스트를 반환합니다.");
            return Collections.emptyList();
        }
    }

    // --- Helper methods for generating trend data (Mock 데이터 생성용) ---
    private <T> List<DateBasedValueDTO<T>> generateDateBasedTrend(LocalDate startDate, LocalDate endDate, java.util.function.Supplier<T> valueSupplier) {
        List<DateBasedValueDTO<T>> trend = new ArrayList<>();
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 0 || days > 366) days = Math.max(1, Math.min(days, 30)); // 데이터 기간 제한
        for (int i = 0; i < days; i++) {
            trend.add(new DateBasedValueDTO<>(startDate.plusDays(i), valueSupplier.get()));
        }
        return trend;
    }

    private <T> List<MonthBasedValueDTO<T>> generateMonthBasedTrend(LocalDate startDate, LocalDate endDate, java.util.function.Supplier<T> valueSupplier) {
        List<MonthBasedValueDTO<T>> trend = new ArrayList<>();
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);
        long months = ChronoUnit.MONTHS.between(startMonth, endMonth) + 1;
        if (months <= 0 || months > 24) months = Math.max(1, Math.min(months, 12)); // 데이터 기간 제한
        for (int i = 0; i < months; i++) {
            trend.add(new MonthBasedValueDTO<>(startMonth.plusMonths(i), valueSupplier.get()));
        }
        return trend;
    }
}
