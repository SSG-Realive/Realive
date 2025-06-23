// src/main/java/com/realive/service/admin/logs/StatService.java
package com.realive.service.admin.logs;

import com.realive.dto.logs.AdminDashboardDTO;
import com.realive.dto.logs.salessum.CategorySalesSummaryDTO;
import com.realive.dto.logs.salessum.DailySalesSummaryDTO;
import com.realive.dto.logs.salessum.MonthlySalesLogDetailListDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.dto.logs.salessum.SalesLogDetailListDTO;
import com.realive.dto.logs.stats.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 관리자 대시보드 및 통계 관련 서비스 인터페이스입니다.
 * 다양한 통계 데이터를 조회하고 집계하는 기능을 정의합니다.
 */
public interface StatService {

    // === 새로운 통합 대시보드 및 기간별 통계 메소드 ===

    /**
     * 지정된 날짜와 기간 타입에 따른 관리자 메인 대시보드 정보를 조회합니다.
     * 이 DTO는 다양한 요약 정보(회원, 판매, 경매, 리뷰 등)와 상품 로그, 패널티 로그 등을 포함합니다.
     *
     * @param date 조회 기준일. "DAILY"의 경우 해당 일자, "MONTHLY"의 경우 해당 월의 임의의 날짜 (해당 월 전체를 기준으로 통계).
     * @param periodType 조회 기간 타입 (예: "DAILY", "MONTHLY"). "WEEKLY"는 현재 미지원.
     * @return {@link AdminDashboardDTO} 관리자 메인 대시보드에 필요한 모든 요약 통계 데이터.
     */
    AdminDashboardDTO getAdminDashboard(LocalDate date, String periodType);

    /**
     * 특정 기간 동안의 판매 통계 정보를 조회합니다.
     * 상품별/판매자별 판매 건수, 매출 정보 및 일별 매출 추이를 포함합니다.
     *
     * @param startDate 조회 시작일.
     * @param endDate 조회 종료일.
     * @param sellerId 특정 판매자에 대한 필터링을 위한 판매자 ID (선택 사항).
     * @param sortBy 정렬 기준 문자열 (예: "revenue_desc" - 매출 내림차순, "quantity_desc" - 판매량 내림차순) (선택 사항).
     * @return {@link SalesPeriodStatsDTO} 기간별 판매 통계 데이터.
     */
    SalesPeriodStatsDTO getSalesStatistics(LocalDate startDate, LocalDate endDate, Optional<Integer> sellerId, Optional<String> sortBy);

    /**
     * 특정 기간 동안의 경매 통계 정보를 조회합니다.
     * 경매 건수, 총 입찰 수, 평균 입찰 수, 낙찰률, 유찰률 등의 요약 통계와 관련 추이 데이터를 포함합니다.
     *
     * @param startDate 조회 시작일.
     * @param endDate 조회 종료일.
     * @return {@link AuctionPeriodStatsDTO} 기간별 경매 통계 데이터.
     */
    AuctionPeriodStatsDTO getAuctionPeriodStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 특정 기간 동안의 회원 통계 정보를 조회합니다.
     * 전체 회원 수, 기간 내 신규 가입자 수, 고유 방문자 수, 참여 사용자 수, 활동 사용자 수 등의 요약 통계와
     * 신규 가입자 및 활동 사용자(정의된 기준의)에 대한 일별/월별 추이 데이터를 포함합니다.
     *
     * @param startDate 조회 시작일.
     * @param endDate 조회 종료일.
     * @return {@link MemberPeriodStatsDTO} 기간별 회원 통계 데이터.
     */
    MemberPeriodStatsDTO getMemberPeriodStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 특정 기간 동안의 리뷰 통계 정보를 조회합니다.
     * 기간 내 총 리뷰 수, 신규 리뷰 수, 평균 평점, 평점 분포, 삭제율 등의 요약 통계와 일별 리뷰 수 추이 데이터를 포함합니다.
     *
     * @param startDate 조회 시작일.
     * @param endDate 조회 종료일.
     * @return {@link ReviewPeriodStatsDTO} 기간별 리뷰 통계 데이터.
     */
    ReviewPeriodStatsDTO getReviewPeriodStatistics(LocalDate startDate, LocalDate endDate);


    // === 기존 메소드들 (점진적으로 위 새로운 통합 메소드들로 대체되거나, 세부 조회용으로 유지될 수 있음) ===

    /**
     * 특정 날짜의 일별 판매 요약 정보(총 판매 건수, 금액, 수량 등)를 조회합니다.
     *
     * @param date 조회할 날짜.
     * @return {@link DailySalesSummaryDTO} 해당 날짜의 판매 요약 정보.
     */
    DailySalesSummaryDTO getDailySalesSummary(LocalDate date);

    /**
     * 특정 날짜의 상세 판매 로그 리스트를 조회합니다.
     *
     * @param date 조회할 날짜.
     * @return {@link SalesLogDetailListDTO} 해당 날짜의 판매 로그 상세 내역 리스트.
     */
    SalesLogDetailListDTO getDailySalesLogDetails(LocalDate date);

    /**
     * 특정 연월의 월별 판매 요약 정보(총 판매 건수, 금액, 수량 등)를 조회합니다.
     *
     * @param yearMonth 조회할 연월 ({@link YearMonth} 객체).
     * @return {@link MonthlySalesSummaryDTO} 해당 연월의 판매 요약 정보.
     */
    MonthlySalesSummaryDTO getMonthlySalesSummary(YearMonth yearMonth);

    /**
     * 특정 연월의 상세 판매 로그 리스트를 조회합니다.
     *
     * @param yearMonth 조회할 연월 ({@link YearMonth} 객체).
     * @return {@link MonthlySalesLogDetailListDTO} 해당 연월의 판매 로그 상세 내역 리스트.
     */
    MonthlySalesLogDetailListDTO getMonthlySalesLogDetails(YearMonth yearMonth);

    /**
     * 특정 연월에 해당하는 모든 날짜의 일별 판매 요약 정보 리스트를 조회합니다.
     *
     * @param yearMonth 조회할 연월 ({@link YearMonth} 객체).
     * @return {@link List<DailySalesSummaryDTO>} 해당 연월의 일별 판매 요약 정보 리스트.
     */
    List<DailySalesSummaryDTO> getDailySummariesInMonth(YearMonth yearMonth);

    /**
     * 특정 판매자의 특정 날짜에 대한 일별 판매 요약 정보를 조회합니다.
     *
     * @param sellerId 조회할 판매자의 ID.
     * @param date 조회할 날짜.
     * @return {@link DailySalesSummaryDTO} 해당 판매자의 해당 날짜 판매 요약 정보.
     */
    DailySalesSummaryDTO getSellerDailySalesSummary(Integer sellerId, LocalDate date);

    /**
     * 특정 판매자의 특정 연월에 대한 월별 판매 요약 정보를 조회합니다.
     *
     * @param sellerId 조회할 판매자의 ID.
     * @param yearMonth 조회할 연월 ({@link YearMonth} 객체).
     * @return {@link MonthlySalesSummaryDTO} 해당 판매자의 해당 연월 판매 요약 정보.
     */
    MonthlySalesSummaryDTO getSellerMonthlySalesSummary(Integer sellerId, YearMonth yearMonth);

    /**
     * 특정 상품의 특정 날짜에 대한 일별 판매 요약 정보를 조회합니다.
     *
     * @param productId 조회할 상품의 ID.
     * @param date 조회할 날짜.
     * @return {@link DailySalesSummaryDTO} 해당 상품의 해당 날짜 판매 요약 정보.
     */
    DailySalesSummaryDTO getProductDailySalesSummary(Integer productId, LocalDate date);

    /**
     * 특정 상품의 특정 연월에 대한 월별 판매 요약 정보를 조회합니다.
     *
     * @param productId 조회할 상품의 ID.
     * @param yearMonth 조회할 연월 ({@link YearMonth} 객체).
     * @return {@link MonthlySalesSummaryDTO} 해당 상품의 해당 연월 판매 요약 정보.
     */
    MonthlySalesSummaryDTO getProductMonthlySalesSummary(Integer productId, YearMonth yearMonth);

    /**
     * 특정 기간 동안의 플랫폼 전체 카테고리별 판매 요약을 조회합니다.
     * 각 카테고리별 판매 건수 및 금액 등을 포함합니다.
     *
     * @param startDate 조회 시작일.
     * @param endDate 조회 종료일.
     * @return {@link List<CategorySalesSummaryDTO>} 카테고리별 판매 요약 DTO 리스트.
     */
    List<CategorySalesSummaryDTO> getPlatformCategorySalesSummary(LocalDate startDate, LocalDate endDate);

    /**
     * 특정 날짜를 기준으로 대시보드에 필요한 통합 통계 정보를 조회합니다. (Map 형식 반환)
     * 이 메소드는 향후 {@link #getAdminDashboard(LocalDate, String)} 메소드로 대체될 수 있습니다.
     *
     * @param date 조회 기준일.
     * @return 다양한 통계 정보를 담은 {@link Map<String, Object>}.
     * @deprecated {@link #getAdminDashboard(LocalDate, String)} 메서드 사용을 권장합니다.
     *             이 메소드는 레거시 호환성 또는 특정 Map 기반 데이터 처리 용도로만 제한적으로 사용되어야 합니다.
     */
    @Deprecated
    Map<String, Object> getDashboardStats(LocalDate date);

    // 기존 메소드들은 그대로 두고 아래 메소드들만 추가
    List<DailySalesSummaryDTO> getDailySummariesForPeriod(LocalDate startDate, LocalDate endDate);
    List<SellerSalesDetailDTO> getSellerSalesDetailsForPeriod(LocalDate startDate, LocalDate endDate);
}
