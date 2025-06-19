// src/main/java/com/realive/service/admin/logs/StatServiceImpl.java
package com.realive.serviceimpl.admin.log;

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
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.logs.CommissionLogRepository;
import com.realive.repository.logs.PayoutLogRepository;
import com.realive.repository.logs.PenaltyLogRepository;
import com.realive.repository.logs.SalesLogRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.seller.SellerRepository;
// import com.realive.repository.user.UserRepository;

// --- DTO stats 패키지 import ---
// 아래 패키지에 SalesPeriodStatsDTO, SellerSalesDetailDTO 등이 수정된 버전으로 있다고 가정
import com.realive.dto.logs.stats.*;

import com.realive.service.admin.logs.StatService;
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
    private final CustomerRepository customerRepository;

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

        // 1. 승인 대기 판매자 수
        int pendingSellerCount = 0;
        if (approvalRepository != null) {
            List<Seller> pendingSellers = approvalRepository.findByIsApprovedFalseAndApprovedAtIsNull();
            pendingSellerCount = pendingSellers.size();
        }

        // 2. 판매 및 커미션 데이터
        List<SalesWithCommissionDTO> salesWithCommissions = new ArrayList<>();
        List<SalesLog> dailySalesLogs = salesLogRepository.findBySoldAt(date);
        for (SalesLog sale : dailySalesLogs) {
            SalesLogDTO salesLogDTO = SalesLogDTO.fromEntity(sale);
            Optional<CommissionLog> commissionOpt = commissionLogRepository.findBySalesLogId(sale.getId());
            CommissionLogDTO commissionLogDTO = commissionOpt.map(CommissionLogDTO::fromEntity).orElse(null);
            salesWithCommissions.add(SalesWithCommissionDTO.builder()
                    .salesLog(salesLogDTO)
                    .commissionLog(commissionLogDTO)
                    .build());
        }

        // 3. 정산 데이터
        List<PayoutLogDTO> payoutLogs = new ArrayList<>();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<PayoutLog> payoutEntities = payoutLogRepository.findByProcessedAtBetween(startOfDay, endOfDay);
        payoutLogs = payoutEntities.stream()
                .map(PayoutLogDTO::fromEntity)
                .collect(Collectors.toList());

        ProductLogDTO productLog = ProductLogDTO.builder()
                .salesWithCommissions(salesWithCommissions)
                .payoutLogs(payoutLogs)
                .build();

        // 4. 패널티 데이터
        List<PenaltyLogDTO> penaltyLogs = new ArrayList<>();
        List<PenaltyLog> penaltyEntities = penaltyLogRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        penaltyLogs = penaltyEntities.stream()
                .map(PenaltyLogDTO::fromEntity)
                .collect(Collectors.toList());

        // 5. 회원 통계
        long totalSellers = sellerRepository.count();
        long activeSellers = sellerRepository.countByIsActiveTrue();
        long inactiveSellers = sellerRepository.countByIsActiveFalse();
        long totalCustomers = customerRepository.count();
        long activeCustomers = customerRepository.countActiveUsers();
        long inactiveCustomers = customerRepository.countInactiveUsers();

        long activeMembers = activeSellers + activeCustomers;
        long inactiveMembers = inactiveSellers + inactiveCustomers;

        MemberSummaryStatsDTO memberSummaryStats = MemberSummaryStatsDTO.builder()
                .totalMembers(totalSellers + totalCustomers)
                .activeMembers(activeMembers)
                .inactiveMembers(inactiveMembers)
                .newMembersInPeriod(0L)
                .uniqueVisitorsInPeriod(0L)
                .engagedUsersInPeriod(0L)
                .activeUsersInPeriod(activeSellers + activeCustomers)
                .build();

        // 6. 판매 통계
        Integer totalSalesAmount = salesLogRepository.sumTotalPriceBySoldAtBetween(startDate, endDate);
        Long totalOrders = salesLogRepository.countDistinctOrdersBySoldAtBetween(startDate, endDate);
        Integer totalFees = commissionLogRepository.sumCommissionAmountBySellerAndDateRange(null, startDate, endDate);

        SalesSummaryStatsDTO salesSummaryStats = SalesSummaryStatsDTO.builder()
                .totalOrdersInPeriod(totalOrders != null ? totalOrders : 0L)
                .totalRevenueInPeriod(totalSalesAmount != null ? totalSalesAmount.doubleValue() : 0.0)
                .totalFeesInPeriod(totalFees != null ? totalFees.doubleValue() : 0.0)
                .build();

        // 7. 경매 통계 (TODO: 실제 경매 관련 Repository 구현 필요)
        AuctionSummaryStatsDTO auctionSummaryStats = AuctionSummaryStatsDTO.builder()
                .totalAuctionsInPeriod(0L)
                .totalBidsInPeriod(0L)
                .averageBidsPerAuctionInPeriod(0.0)
                .successRate(0.0)
                .failureRate(0.0)
                .build();

        // 8. 리뷰 통계 (TODO: 실제 리뷰 관련 Repository 구현 필요)
        ReviewSummaryStatsDTO reviewSummaryStats = ReviewSummaryStatsDTO.builder()
                .totalReviewsInPeriod(0L)
                .newReviewsInPeriod(0L)
                .averageRatingInPeriod(0.0)
                .deletionRate(0.0)
                .build();

        return AdminDashboardDTO.builder()
                .queryDate(date)
                .periodType(periodType.toUpperCase())
                .pendingSellerCount(pendingSellerCount)
                .productLog(productLog)
                .penaltyLogs(penaltyLogs)
                .memberSummaryStats(memberSummaryStats)
                .salesSummaryStats(salesSummaryStats)
                .auctionSummaryStats(auctionSummaryStats)
                .reviewSummaryStats(reviewSummaryStats)
                .build();
    }

    @Override
    public SalesPeriodStatsDTO getSalesStatistics(LocalDate startDate, LocalDate endDate,
                                                  Optional<Integer> sellerId, Optional<String> sortBy) {
        log.info("getSalesStatistics 호출됨 (판매자별 집중) - 기간: {} ~ {}, 판매자ID: {}, 정렬: {}", startDate, endDate, sellerId, sortBy);

        // 전체 판매 요약 정보 (선택적으로 유지)
        // TODO: 실제 데이터 조회 시, sellerId 유무에 따라 요약 범위 달라질 수 있음
        SalesSummaryStatsDTO summary = SalesSummaryStatsDTO.builder()
                .totalOrdersInPeriod(ThreadLocalRandom.current().nextLong(sellerId.isPresent() ? 10 : 100, sellerId.isPresent() ? 50 : 1000))
                .totalRevenueInPeriod(ThreadLocalRandom.current().nextDouble(sellerId.isPresent() ? 1000000 : 10000000, sellerId.isPresent() ? 5000000 : 50000000))
                .totalFeesInPeriod(ThreadLocalRandom.current().nextDouble(100000, 5000000)) // 이 수수료는 전체 플랫폼 수수료일 수 있음
                .build();

        // 판매자별 판매 상세 정보 생성
        List<SellerSalesDetailDTO> sellerDetails = new ArrayList<>();
        if (sellerId.isPresent()) {
            // 특정 판매자 조회 시
            // TODO: 실제 DB에서 해당 sellerId의 판매 건수(salesCount)와 매출(totalRevenue) 조회
            sellerDetails.add(SellerSalesDetailDTO.builder()
                    .sellerId(sellerId.get())
                    .sellerName("Mock 판매자 " + sellerId.get()) // TODO: 실제 판매자 이름 조회
                    .salesCount(ThreadLocalRandom.current().nextLong(5, 50)) // 판매 건수
                    .totalRevenue(ThreadLocalRandom.current().nextDouble(500000, 5000000))
                    .build());
        } else {
            // 전체 판매자 (또는 상위 판매자 등) 조회 시
            // TODO: 실제 DB에서 판매자별 판매 건수와 매출 조회 (페이징 또는 Top N 고려)
            for(int i = 0; i < ThreadLocalRandom.current().nextInt(3, 8); i++) {
                sellerDetails.add(SellerSalesDetailDTO.builder()
                        .sellerId(i + 1)
                        .sellerName("Mock 판매자 " + (i + 1)) // TODO: 실제 판매자 이름 조회
                        .salesCount(ThreadLocalRandom.current().nextLong(10, 100)) // 판매 건수
                        .totalRevenue(ThreadLocalRandom.current().nextDouble(500000, 5000000))
                        .build());
            }
            // sortBy 로직 (판매자별 정렬) - salesCount 또는 totalRevenue 기준
            sortBy.ifPresent(s -> {
                if (s.equalsIgnoreCase("salesCount_desc")) {
                    sellerDetails.sort(Comparator.comparing(SellerSalesDetailDTO::getSalesCount).reversed());
                } else if (s.equalsIgnoreCase("revenue_desc")) {
                    sellerDetails.sort(Comparator.comparing(SellerSalesDetailDTO::getTotalRevenue).reversed());
                }
                // 다른 정렬 기준 추가 가능
            });
        }

        // 전체 매출 추이 (선택적으로 유지)
        // TODO: 실제 데이터 조회. sellerId가 있다면 해당 판매자의 매출 추이, 없다면 전체 매출 추이
        List<DateBasedValueDTO<Double>> dailyRevenueTrend = generateDateBasedTrend(startDate, endDate,
                () -> ThreadLocalRandom.current().nextDouble(100000, 1000000));

        return SalesPeriodStatsDTO.builder()
                .summary(summary) // 전체 요약 (선택적)
                .sellerSalesDetails(sellerDetails) // 핵심: 판매자별 상세
                .dailyRevenueTrend(dailyRevenueTrend) // 전체 또는 판매자별 매출 추이 (선택적)
                .build();
    }

    @Override
    public AuctionPeriodStatsDTO getAuctionPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        // (이전 최종본과 동일 - 내용 생략)
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
        // (이전 최종본과 동일 - MemberPeriodStatsDTO의 최종 정의 반영됨)
        // 여기서는 해당 코드 반복을 피하기 위해 생략합니다.
        // 이전 답변의 getMemberPeriodStatistics 메소드 구현을 참고해주세요.
        // 핵심: MemberSummaryStatsDTO에 3가지 활동 지표 채우고,
        // MemberPeriodStatsDTO에는 dailyNewUserTrend, dailyActiveUserTrend, monthlyNewUserTrend, monthlyActiveUserTrend만 채움
        log.info("getMemberPeriodStatistics 호출됨 - 기간: {} ~ {}", startDate, endDate);

        MemberSummaryStatsDTO summary = MemberSummaryStatsDTO.builder()
                .totalMembers(15000L + ThreadLocalRandom.current().nextLong(2000))
                .newMembersInPeriod(ThreadLocalRandom.current().nextLong(100, 500))
                .uniqueVisitorsInPeriod(ThreadLocalRandom.current().nextLong(2000, 8000))
                .engagedUsersInPeriod(ThreadLocalRandom.current().nextLong(1000, 4000))
                .activeUsersInPeriod(ThreadLocalRandom.current().nextLong(1500, 6000))
                .build();

        List<DateBasedValueDTO<Long>> dailyNewUserTrend = generateDateBasedTrend(startDate, endDate,
                () -> ThreadLocalRandom.current().nextLong(5, 30));
        List<DateBasedValueDTO<Long>> dailyActiveUserTrend = generateDateBasedTrend(startDate, endDate,
                () -> {
                    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    if (daysBetween <= 0) return summary.getActiveUsersInPeriod();
                    return summary.getActiveUsersInPeriod() / daysBetween + ThreadLocalRandom.current().nextLong(-20, 21);
                });

        List<MonthBasedValueDTO<Long>> monthlyNewUserTrend = generateMonthBasedTrend(startDate, endDate,
                () -> ThreadLocalRandom.current().nextLong(100, 500));
        List<MonthBasedValueDTO<Long>> monthlyActiveUserTrend = generateMonthBasedTrend(startDate, endDate,
                () -> {
                    long monthsBetween = ChronoUnit.MONTHS.between(YearMonth.from(startDate), YearMonth.from(endDate)) + 1;
                    if (monthsBetween <= 0) return summary.getActiveUsersInPeriod();
                    return summary.getActiveUsersInPeriod() / monthsBetween + ThreadLocalRandom.current().nextLong(-200, 201);
                });

        return MemberPeriodStatsDTO.builder()
                .summary(summary)
                .dailyNewUserTrend(dailyNewUserTrend)
                .dailyActiveUserTrend(dailyActiveUserTrend)
                .monthlyNewUserTrend(monthlyNewUserTrend)
                .monthlyActiveUserTrend(monthlyActiveUserTrend)
                .build();
    }

    @Override
    public ReviewPeriodStatsDTO getReviewPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        // (이전 최종본과 동일 - 내용 생략)
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

    // === 기존 메소드들 (변경 없음 - 이전 최종본과 동일, 내용 생략) ===
    @Override
    public DailySalesSummaryDTO getDailySalesSummary(LocalDate date) { /* 이전과 동일 */
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
    public SalesLogDetailListDTO getDailySalesLogDetails(LocalDate date) { /* 이전과 동일 */
        log.info("getDailySalesLogDetails 호출됨 - 날짜: {}", date);
        List<SalesLog> salesLogEntities = salesLogRepository.findBySoldAt(date);
        List<SalesLogDTO> salesLogDTOs = salesLogEntities.stream().map(SalesLogDTO::fromEntity).collect(Collectors.toList());
        return SalesLogDetailListDTO.builder().date(date).salesLogs(salesLogDTOs).build();
    }
    @Override
    public MonthlySalesSummaryDTO getMonthlySalesSummary(YearMonth yearMonth) { /* 이전과 동일 */
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
    public MonthlySalesLogDetailListDTO getMonthlySalesLogDetails(YearMonth yearMonth) { /* 이전과 동일 */
        log.info("getMonthlySalesLogDetails 호출됨 - 연월: {}", yearMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<SalesLog> salesLogEntities = salesLogRepository.findBySoldAtBetween(startDate, endDate);
        List<SalesLogDTO> salesLogDTOs = salesLogEntities.stream().map(SalesLogDTO::fromEntity).collect(Collectors.toList());
        return MonthlySalesLogDetailListDTO.builder().month(yearMonth).salesLogs(salesLogDTOs).build();
    }
    @Override
    public List<DailySalesSummaryDTO> getDailySummariesInMonth(YearMonth yearMonth) { /* 이전과 동일 */
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
    public DailySalesSummaryDTO getSellerDailySalesSummary(Integer sellerId, LocalDate date) { /* 이전과 동일 */
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
    public MonthlySalesSummaryDTO getSellerMonthlySalesSummary(Integer sellerId, YearMonth yearMonth) { /* 이전과 동일 */
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
    public DailySalesSummaryDTO getProductDailySalesSummary(Integer productId, LocalDate date) { /* 이전과 동일 */
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
    public MonthlySalesSummaryDTO getProductMonthlySalesSummary(Integer productId, YearMonth yearMonth) { /* 이전과 동일 */
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
    public Map<String, Object> getDashboardStats(LocalDate date) { /* 이전과 동일 */
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
    public List<CategorySalesSummaryDTO> getPlatformCategorySalesSummary(LocalDate startDate, LocalDate endDate) { /* 이전과 동일 */
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
        if (days <= 0 || days > 366) days = Math.max(1, Math.min(days, 30));
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
        if (months <= 0 || months > 24) months = Math.max(1, Math.min(months, 12));
        for (int i = 0; i < months; i++) {
            trend.add(new MonthBasedValueDTO<>(startMonth.plusMonths(i), valueSupplier.get()));
        }
        return trend;
    }
}
