package com.realive.service.admin.logs;

import com.realive.domain.logs.CommissionLog;
import com.realive.domain.logs.PayoutLog;
import com.realive.domain.logs.PenaltyLog;
import com.realive.domain.logs.SalesLog;
// import com.realive.domain.product.Product; // ProductRepository를 사용하므로 Product 엔티티도 필요할 수 있음
import com.realive.domain.seller.Seller;
import com.realive.dto.logs.AdminDashboardDTO;
import com.realive.dto.logs.CommissionLogDTO;
import com.realive.dto.logs.PayoutLogDTO;
import com.realive.dto.logs.PenaltyLogDTO;
import com.realive.dto.logs.ProductLogDTO;
import com.realive.dto.logs.SalesLogDTO;
import com.realive.dto.logs.SalesWithCommissionDTO;
import com.realive.dto.logs.salessum.DailySalesSummaryDTO;
import com.realive.dto.logs.salessum.MonthlySalesLogDetailListDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.dto.logs.salessum.SalesLogDetailListDTO;
import com.realive.dto.logs.salessum.CategorySalesSummaryDTO;
import com.realive.repository.admin.approval.ApprovalRepository;
import com.realive.repository.logs.CommissionLogRepository;
import com.realive.repository.logs.PayoutLogRepository;
import com.realive.repository.logs.PenaltyLogRepository;
import com.realive.repository.logs.SalesLogRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.seller.SellerRepository; // SellerRepository 주입
// import com.realive.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;

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
    private final SellerRepository sellerRepository; // SellerRepository 주입
    // private final UserRepository userRepository;

    // 일별 통계
    @Override
    public DailySalesSummaryDTO getDailySalesSummary(LocalDate date) {
        log.info("getDailySalesSummary 호출됨 - 날짜: {}", date);

        Integer salesCount = salesLogRepository.countBySoldAt(date);
        Integer salesAmount = salesLogRepository.sumTotalPriceByDate(date);
        Integer quantitySum = salesLogRepository.sumQuantityByDate(date);

        int totalSalesCount = (salesCount != null) ? salesCount : 0;
        int totalSalesAmount = (salesAmount != null) ? salesAmount : 0;
        int totalQuantity = (quantitySum != null) ? quantitySum : 0;

        return DailySalesSummaryDTO.builder()
                .date(date)
                .totalSalesCount(totalSalesCount)
                .totalSalesAmount(totalSalesAmount)
                .totalQuantity(totalQuantity)
                .build();
    }

    @Override
    public SalesLogDetailListDTO getDailySalesLogDetails(LocalDate date) {
        log.info("getDailySalesLogDetails 호출됨 - 날짜: {}", date);
        List<SalesLog> salesLogEntities = salesLogRepository.findBySoldAt(date);
        List<SalesLogDTO> salesLogDTOs = salesLogEntities.stream()
                .map(SalesLogDTO::fromEntity)
                .collect(Collectors.toList());
        return SalesLogDetailListDTO.builder()
                .date(date)
                .salesLogs(salesLogDTOs)
                .build();
    }

    // 월별 통계
    @Override
    public MonthlySalesSummaryDTO getMonthlySalesSummary(YearMonth yearMonth) {
        log.info("getMonthlySalesSummary 호출됨 - 연월: {}", yearMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Integer salesAmount = salesLogRepository.sumTotalPriceBySoldAtBetween(startDate, endDate);
        Long orderCountFromRepo = salesLogRepository.countDistinctOrdersBySoldAtBetween(startDate, endDate);
        Integer quantitySum = salesLogRepository.sumQuantityBySoldAtBetween(startDate, endDate);

        int totalSalesAmount = (salesAmount != null) ? salesAmount : 0;
        int totalSalesCount = (orderCountFromRepo != null) ? orderCountFromRepo.intValue() : 0;
        int totalQuantity = (quantitySum != null) ? quantitySum : 0;

        return MonthlySalesSummaryDTO.builder()
                .month(yearMonth)
                .totalSalesCount(totalSalesCount)
                .totalSalesAmount(totalSalesAmount)
                .totalQuantity(totalQuantity)
                .build();
    }

    @Override
    public MonthlySalesLogDetailListDTO getMonthlySalesLogDetails(YearMonth yearMonth) {
        log.info("getMonthlySalesLogDetails 호출됨 - 연월: {}", yearMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<SalesLog> salesLogEntities = salesLogRepository.findBySoldAtBetween(startDate, endDate);
        List<SalesLogDTO> salesLogDTOs = salesLogEntities.stream()
                .map(SalesLogDTO::fromEntity)
                .collect(Collectors.toList());
        return MonthlySalesLogDetailListDTO.builder()
                .month(yearMonth)
                .salesLogs(salesLogDTOs)
                .build();
    }

    @Override
    public List<DailySalesSummaryDTO> getDailySummariesInMonth(YearMonth yearMonth) {
        log.info("getDailySummariesInMonth 호출됨 (for 루프 사용) - 연월: {}", yearMonth);
        List<DailySalesSummaryDTO> dailySummaries = new ArrayList<>();
        int daysInMonth = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = yearMonth.atDay(day);
            DailySalesSummaryDTO dailySalesSummary = getDailySalesSummary(currentDate);
            if (dailySalesSummary != null) {
                dailySummaries.add(dailySalesSummary);
            } else {
                log.warn("{} 날짜의 DailySalesSummary가 null이므로 기본값(0)으로 처리된 DTO를 추가합니다.", currentDate);
                dailySummaries.add(DailySalesSummaryDTO.builder()
                        .date(currentDate)
                        .totalSalesCount(0)
                        .totalSalesAmount(0)
                        .totalQuantity(0)
                        .build());
            }
        }
        return dailySummaries;
    }

    // 판매자별 통계
    @Override
    public DailySalesSummaryDTO getSellerDailySalesSummary(Integer sellerId, LocalDate date) {
        log.info("getSellerDailySalesSummary 호출됨 - 판매자ID: {}, 날짜: {}", sellerId, date);
        Integer salesCount = salesLogRepository.countBySellerIdAndSoldAt(sellerId, date);
        Integer salesAmount = salesLogRepository.sumTotalPriceBySellerIdAndSoldAt(sellerId, date);
        Integer quantitySum = salesLogRepository.sumQuantityBySellerIdAndSoldAt(sellerId, date);

        int totalSalesCount = (salesCount != null) ? salesCount : 0;
        int totalSalesAmount = (salesAmount != null) ? salesAmount : 0;
        int totalQuantity = (quantitySum != null) ? quantitySum : 0;

        return DailySalesSummaryDTO.builder()
                .date(date)
                .totalSalesCount(totalSalesCount)
                .totalSalesAmount(totalSalesAmount)
                .totalQuantity(totalQuantity)
                .build();
    }

    @Override
    public MonthlySalesSummaryDTO getSellerMonthlySalesSummary(Integer sellerId, YearMonth yearMonth) {
        log.info("getSellerMonthlySalesSummary 호출됨 - 판매자ID: {}, 연월: {}", sellerId, yearMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Integer salesAmount = salesLogRepository.sumTotalPriceBySellerIdAndSoldAtBetween(sellerId, startDate, endDate);
        Long orderCountFromRepo = salesLogRepository.countDistinctOrdersBySellerIdAndSoldAtBetween(sellerId, startDate, endDate);
        Integer quantitySum = salesLogRepository.sumQuantityBySellerIdAndSoldAtBetween(sellerId, startDate, endDate);

        int totalSalesAmount = (salesAmount != null) ? salesAmount : 0;
        int totalSalesCount = (orderCountFromRepo != null) ? orderCountFromRepo.intValue() : 0;
        int totalQuantity = (quantitySum != null) ? quantitySum : 0;

        return MonthlySalesSummaryDTO.builder()
                .month(yearMonth)
                .totalSalesCount(totalSalesCount)
                .totalSalesAmount(totalSalesAmount)
                .totalQuantity(totalQuantity)
                .build();
    }

    // 상품별 통계
    @Override
    public DailySalesSummaryDTO getProductDailySalesSummary(Integer productId, LocalDate date) {
        log.info("getProductDailySalesSummary 호출됨 - 상품ID: {}, 날짜: {}", productId, date);
        Integer salesAmount = salesLogRepository.sumTotalPriceByProductIdAndSoldAtBetween(productId, date, date);
        Integer quantitySum = salesLogRepository.sumQuantityByProductIdAndSoldAtBetween(productId, date, date);
        Integer salesCount = salesLogRepository.countByProductIdAndSoldAt(productId, date);

        int totalSalesAmount = (salesAmount != null) ? salesAmount : 0;
        int totalQuantity = (quantitySum != null) ? quantitySum : 0;
        int totalSalesCount = (salesCount != null) ? salesCount : 0;

        return DailySalesSummaryDTO.builder()
                .date(date)
                .totalSalesCount(totalSalesCount)
                .totalSalesAmount(totalSalesAmount)
                .totalQuantity(totalQuantity)
                .build();
    }

    @Override
    public MonthlySalesSummaryDTO getProductMonthlySalesSummary(Integer productId, YearMonth yearMonth) {
        log.info("getProductMonthlySalesSummary 호출됨 - 상품ID: {}, 연월: {}", productId, yearMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Integer salesAmount = salesLogRepository.sumTotalPriceByProductIdAndSoldAtBetween(productId, startDate, endDate);
        Integer quantitySum = salesLogRepository.sumQuantityByProductIdAndSoldAtBetween(productId, startDate, endDate);
        Integer salesCount = salesLogRepository.countByProductIdAndSoldAtBetween(productId, startDate, endDate);

        int totalSalesAmount = (salesAmount != null) ? salesAmount : 0;
        int totalQuantity = (quantitySum != null) ? quantitySum : 0;
        int totalSalesCount = (salesCount != null) ? salesCount : 0;

        return MonthlySalesSummaryDTO.builder()
                .month(yearMonth)
                .totalSalesCount(totalSalesCount)
                .totalSalesAmount(totalSalesAmount)
                .totalQuantity(totalQuantity)
                .build();
    }


    // 대시보드용 통합 통계
    @Override
    public Map<String, Object> getDashboardStats(LocalDate date) {
        log.info("관리자 대시보드 통합 통계 조회 (Map 반환) - 날짜: {}", date);
        Map<String, Object> dashboardData = new HashMap<>();

        // 1. 승인 대기 중인 판매자 수 계산
        List<Seller> pendingSellers = approvalRepository.findByIsApprovedFalseAndApprovedAtIsNull();
        int pendingSellerCount = pendingSellers.size();
        log.debug("승인 대기 중인 판매자 수: {}", pendingSellerCount);

        // 2. 상품 요약 정보 (총 상품 수, 오늘 등록 상품 수)
        long totalProductsCount = 0L;
        long newProductsTodayCount = 0L;
        if (productRepository != null) {
            totalProductsCount = productRepository.count();
            LocalDateTime startOfDayForProduct = date.atStartOfDay();
            LocalDateTime tomorrowStartOfDay = date.plusDays(1).atStartOfDay();
            newProductsTodayCount = productRepository.countByCreatedAtBetween(startOfDayForProduct, tomorrowStartOfDay);
        }

        // 3. ProductLogDTO의 salesWithCommissions 채우기
        List<SalesWithCommissionDTO> salesWithCommissionsData = new ArrayList<>();
        if (salesLogRepository != null && commissionLogRepository != null) {
            // N+1 문제 가능성 있음. 실제 운영시에는 최적화된 쿼리 권장.
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
            log.debug("{}일자 판매(수수료 포함) 로그 {}건 처리", date, salesWithCommissionsData.size());
        } else {
            log.warn("salesWithCommissionsData 생성을 위한 리포지토리 중 일부가 null입니다.");
        }

        // 4. ProductLogDTO의 payoutLogs 채우기
        List<PayoutLogDTO> payoutLogDataList = new ArrayList<>();
        if (payoutLogRepository != null) {
            LocalDateTime startOfDayForPayout = date.atStartOfDay();
            LocalDateTime endOfDayForPayout = date.atTime(LocalTime.MAX);
            List<PayoutLog> payoutEntities = payoutLogRepository.findByProcessedAtBetween(startOfDayForPayout, endOfDayForPayout);
            payoutLogDataList = payoutEntities.stream()
                    .map(PayoutLogDTO::fromEntity)
                    .collect(Collectors.toList());
            log.debug("{}일자 정산 로그 {}건 처리", date, payoutLogDataList.size());
        } else {
            log.warn("payoutLogDataList 생성을 위한 PayoutLogRepository가 null입니다.");
        }

        ProductLogDTO productLogData = ProductLogDTO.builder()
                .salesWithCommissions(salesWithCommissionsData)
                .payoutLogs(payoutLogDataList)
                .build();

        // 5. List<PenaltyLogDTO> 데이터 생성 또는 조회
        List<PenaltyLogDTO> penaltyLogDTOList;
        if (penaltyLogRepository != null) {
            LocalDateTime startOfDayForPenalty = date.atStartOfDay();
            LocalDateTime endOfDayForPenalty = date.atTime(LocalTime.MAX);
            List<PenaltyLog> penaltyEntities = penaltyLogRepository.findByCreatedAtBetween(startOfDayForPenalty, endOfDayForPenalty);
            penaltyLogDTOList = penaltyEntities.stream()
                    .map(PenaltyLogDTO::fromEntity)
                    .collect(Collectors.toList());
            log.debug("{}일자 패널티 로그 {}건 조회", date, penaltyLogDTOList.size());
        } else {
            penaltyLogDTOList = Collections.emptyList();
        }

        // 6. AdminDashboardDTO 객체 생성
        AdminDashboardDTO adminViewData = AdminDashboardDTO.builder()
                .productLog(productLogData)
                .penaltyLogs(penaltyLogDTOList)
                .pendingSellerCount(pendingSellerCount)
                .build();

        // 7. Map에 데이터 담기
        dashboardData.put("adminViewData", adminViewData);
        dashboardData.put("totalProducts", totalProductsCount);
        dashboardData.put("newProductsToday", newProductsTodayCount);
        // TODO: 필요시 더 많은 요약 통계 (예: 오늘 총 판매액 등)를 Map에 직접 추가할 수 있음
        // dashboardData.put("todayTotalSalesAmount", getDailySalesSummary(date).getTotalSalesAmount());

        log.info("대시보드 데이터 구성 완료: {}", dashboardData.keySet());
        return dashboardData;


    }

    /**
     * 특정 기간 동안의 플랫폼 전체 카테고리별 판매 요약을 조회합니다.
     * (StatService 인터페이스에 선언된 메소드의 구현)
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 카테고리별 판매 요약 DTO 리스트
     */
    @Override // 인터페이스 메소드를 구현함을 명시
    public List<CategorySalesSummaryDTO> getPlatformCategorySalesSummary(LocalDate startDate, LocalDate endDate) {
        log.info("플랫폼 전체 카테고리별 판매 요약 조회 요청 - 기간: {} ~ {}", startDate, endDate);
        if (salesLogRepository != null) {
            // SalesLogRepository에 해당 기간의 카테고리별 집계 쿼리 메소드가 있다고 가정
            // (이전 답변에서 findCategorySalesSummaryBetween 메소드를 정의했었습니다)
            return salesLogRepository.findCategorySalesSummaryBetween(startDate, endDate);
        } else {
            log.warn("SalesLogRepository가 주입되지 않았습니다. 카테고리별 판매 요약에 대해 빈 리스트를 반환합니다.");
            return Collections.emptyList(); // SalesLogRepository가 null인 경우 빈 리스트 반환
        }
    }
}
