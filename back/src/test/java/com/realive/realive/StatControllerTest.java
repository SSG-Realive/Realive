package com.realive.realive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.realive.controller.admin.AdminStatsController;
import com.realive.dto.common.ApiResponse;
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
import com.realive.service.admin.logs.StatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("deprecation")
@WebMvcTest(AdminStatsController.class)
class StatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatService statService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private DailySalesSummaryDTO mockDailySummary;
    private SalesLogDetailListDTO mockDailyDetails;
    private MonthlySalesSummaryDTO mockMonthlySummary;
    private MonthlySalesLogDetailListDTO mockMonthlyDetails;
    private List<DailySalesSummaryDTO> mockDailySummariesInMonth;
    private List<CategorySalesSummaryDTO> mockCategorySummaries;
    private Map<String, Object> mockDashboardData;
    private SalesLogDTO mockSalesLogDto1;
    private CommissionLogDTO mockCommissionLogDto1;
    private PayoutLogDTO mockPayoutLogDto1;
    private PenaltyLogDTO mockPenaltyLogDto1;

    @BeforeEach
    public void setup() {
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        LocalDate testDate = LocalDate.of(2025, 5, 28);
        YearMonth testYearMonth = YearMonth.of(2025, 5);

        mockDailySummary = DailySalesSummaryDTO.builder().date(testDate).totalSalesCount(10).totalSalesAmount(100000).totalQuantity(20).build();

        mockSalesLogDto1 = SalesLogDTO.builder().id(1).orderItemId(1).productId(101).sellerId(1).customerId(1).quantity(1).unitPrice(10000).totalPrice(10000).soldAt(testDate).build();
        SalesLogDTO mockSalesLogDto2 = SalesLogDTO.builder().id(2).orderItemId(2).productId(101).sellerId(1).customerId(2).quantity(2).unitPrice(10000).totalPrice(20000).soldAt(testDate).build();
        mockDailyDetails = SalesLogDetailListDTO.builder().date(testDate).salesLogs(Arrays.asList(mockSalesLogDto1, mockSalesLogDto2)).build();

        mockMonthlySummary = MonthlySalesSummaryDTO.builder().month(testYearMonth).totalSalesCount(50).totalSalesAmount(500000).totalQuantity(100).build();
        mockMonthlyDetails = MonthlySalesLogDetailListDTO.builder().month(testYearMonth).salesLogs(Arrays.asList(mockSalesLogDto1, mockSalesLogDto2)).build();
        mockDailySummariesInMonth = Arrays.asList(DailySalesSummaryDTO.builder().date(testYearMonth.atDay(1)).totalSalesCount(5).build());
        mockCategorySummaries = Arrays.asList(new CategorySalesSummaryDTO(1L, "가구", 10L, 500000L, 0));

        // Corrected: Removed 'L' from id values to match Integer type
        mockCommissionLogDto1 = CommissionLogDTO.builder().id(1).salesLogId(1).commissionAmount(1000).commissionRate(new BigDecimal("0.1")).build();
        mockPayoutLogDto1 = PayoutLogDTO.builder().id(1).sellerId(1).payoutAmount(8000).processedAt(testDate.atTime(10,0)).build();
        // Corrected: Removed .userId(1) as it's not present in PenaltyLogDTO
        mockPenaltyLogDto1 = PenaltyLogDTO.builder().id(1).reason("지연배송").points(5000).build();

        ProductLogDTO productLog = ProductLogDTO.builder()
                .salesWithCommissions(Arrays.asList(
                        SalesWithCommissionDTO.builder()
                                .salesLog(mockSalesLogDto1)
                                .commissionLog(mockCommissionLogDto1)
                                .build()
                ))
                .payoutLogs(Arrays.asList(mockPayoutLogDto1))
                .build();
        AdminDashboardDTO adminViewData = AdminDashboardDTO.builder()
                .productLog(productLog)
                .penaltyLogs(Arrays.asList(mockPenaltyLogDto1))
                .pendingSellerCount(5)
                .build();
        mockDashboardData = new HashMap<>();
        mockDashboardData.put("adminViewData", adminViewData);
        mockDashboardData.put("totalProducts", 100L);
        mockDashboardData.put("newProductsToday", 10L);
    }

    // --- 관리자 대시보드 통계 API 테스트 ---
    @Test
    @DisplayName("GET /dashboard - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getDashboardStats_withData_shouldReturnOk() throws Exception {
        LocalDate testDate = LocalDate.of(2025, 5, 28);
        given(statService.getDashboardStats(eq(testDate))).willReturn(mockDashboardData);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/dashboard")
                .param("date", testDate.toString())
                .contentType(MediaType.APPLICATION_JSON));

        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.totalProducts").value(100L))
                .andExpect(jsonPath("$.data.adminViewData.pendingSellerCount").value(5))
                .andExpect(jsonPath("$.data.adminViewData.productLog.salesWithCommissions[0].commissionLog.commissionAmount").value(mockCommissionLogDto1.getCommissionAmount()))
                .andExpect(jsonPath("$.data.adminViewData.productLog.payoutLogs[0].payoutAmount").value(mockPayoutLogDto1.getPayoutAmount()))
                .andExpect(jsonPath("$.data.adminViewData.penaltyLogs[0].points").value(mockPenaltyLogDto1.getPoints()));
    }

    // --- 일별 판매 요약 API 테스트 ---
    @Test
    @DisplayName("GET /daily-summary - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getDailySalesSummary_withData_shouldReturnOk() throws Exception {
        LocalDate testDate = LocalDate.of(2025, 5, 28);
        given(statService.getDailySalesSummary(eq(testDate))).willReturn(mockDailySummary);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/daily-summary").param("date", testDate.toString()));
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.data.totalSalesCount").value(mockDailySummary.getTotalSalesCount()));
    }

    // --- 일별 상세 판매 로그 API 테스트 ---
    @Test
    @DisplayName("GET /daily-details - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getDailySalesLogDetails_withData_shouldReturnOk() throws Exception {
        LocalDate testDate = LocalDate.of(2025, 5, 28);
        given(statService.getDailySalesLogDetails(eq(testDate))).willReturn(mockDailyDetails);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/daily-details").param("date", testDate.toString()));
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.data.salesLogs[0].id").value(mockDailyDetails.getSalesLogs().get(0).getId()));
    }

    // --- 월별 판매 요약 API 테스트 ---
    @Test
    @DisplayName("GET /monthly-summary - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getMonthlySalesSummary_withData_shouldReturnOk() throws Exception {
        YearMonth testYearMonth = YearMonth.of(2025, 5);
        given(statService.getMonthlySalesSummary(eq(testYearMonth))).willReturn(mockMonthlySummary);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/monthly-summary").param("yearMonth", testYearMonth.toString()));
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.data.totalSalesAmount").value(mockMonthlySummary.getTotalSalesAmount()));
    }

    // --- 월별 상세 판매 로그 API 테스트 ---
    @Test
    @DisplayName("GET /monthly-details - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getMonthlySalesLogDetails_withData_shouldReturnOk() throws Exception {
        YearMonth testYearMonth = YearMonth.of(2025, 5);
        given(statService.getMonthlySalesLogDetails(eq(testYearMonth))).willReturn(mockMonthlyDetails);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/monthly-details").param("yearMonth", testYearMonth.toString()));
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.data.salesLogs.length()").value(mockMonthlyDetails.getSalesLogs().size()));
    }

    // --- 특정 월의 일별 판매 요약 리스트 API 테스트 ---
    @Test
    @DisplayName("GET /daily-summaries-in-month - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getDailySummariesInMonth_withData_shouldReturnOk() throws Exception {
        YearMonth testYearMonth = YearMonth.of(2025, 5);
        given(statService.getDailySummariesInMonth(eq(testYearMonth))).willReturn(mockDailySummariesInMonth);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/daily-summaries-in-month").param("yearMonth", testYearMonth.toString()));
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(mockDailySummariesInMonth.size()));
    }

    // --- 판매자별 일별 판매 요약 API 테스트 ---
    @Test
    @DisplayName("GET /seller/{sellerId}/daily-summary - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getSellerDailySalesSummary_withData_shouldReturnOk() throws Exception {
        Integer sellerId = 1;
        LocalDate testDate = LocalDate.of(2025, 5, 28);
        given(statService.getSellerDailySalesSummary(eq(sellerId), eq(testDate))).willReturn(mockDailySummary);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/seller/{sellerId}/daily-summary", sellerId).param("date", testDate.toString()));
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.data.totalSalesCount").value(mockDailySummary.getTotalSalesCount()));
    }

    // --- 판매자별 월별 판매 요약 API 테스트 ---
    @Test
    @DisplayName("GET /seller/{sellerId}/monthly-summary - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getSellerMonthlySalesSummary_withData_shouldReturnOk() throws Exception {
        Integer sellerId = 1;
        YearMonth testYearMonth = YearMonth.of(2025, 5);
        given(statService.getSellerMonthlySalesSummary(eq(sellerId), eq(testYearMonth))).willReturn(mockMonthlySummary);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/seller/{sellerId}/monthly-summary", sellerId).param("yearMonth", testYearMonth.toString()));
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.data.totalSalesAmount").value(mockMonthlySummary.getTotalSalesAmount()));
    }

    // --- 상품별 일별 판매 요약 API 테스트 ---
    @Test
    @DisplayName("GET /product/{productId}/daily-summary - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getProductDailySalesSummary_withData_shouldReturnOk() throws Exception {
        Integer productId = 101;
        LocalDate testDate = LocalDate.of(2025, 5, 28);
        given(statService.getProductDailySalesSummary(eq(productId), eq(testDate))).willReturn(mockDailySummary);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/product/{productId}/daily-summary", productId).param("date", testDate.toString()));
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.data.totalSalesCount").value(mockDailySummary.getTotalSalesCount()));
    }

    // --- 상품별 월별 판매 요약 API 테스트 ---
    @Test
    @DisplayName("GET /product/{productId}/monthly-summary - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getProductMonthlySalesSummary_withData_shouldReturnOk() throws Exception {
        Integer productId = 101;
        YearMonth testYearMonth = YearMonth.of(2025, 5);
        given(statService.getProductMonthlySalesSummary(eq(productId), eq(testYearMonth))).willReturn(mockMonthlySummary);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/product/{productId}/monthly-summary", productId).param("yearMonth", testYearMonth.toString()));
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.data.totalSalesAmount").value(mockMonthlySummary.getTotalSalesAmount()));
    }


    // --- 카테고리별 판매 요약 API 테스트 ---
    @Test
    @DisplayName("GET /category-summary - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getPlatformCategorySalesSummary_withData_shouldReturnOk() throws Exception {
        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 28);
        given(statService.getPlatformCategorySalesSummary(eq(startDate), eq(endDate))).willReturn(mockCategorySummaries);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/category-summary")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()));
        actions.andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(mockCategorySummaries.size()));
    }

    // 데이터 없음 (204 또는 404) 및 서비스 오류 (500) 케이스는 각 엔드포인트별로 추가하면 좋습니다.
    // 예시: getDailySalesSummary - 데이터 없음
    @Test
    @DisplayName("GET /daily-summary - 데이터 없음 (404 Not Found) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getDailySalesSummary_noData_shouldReturnNotFound() throws Exception {
        LocalDate testDate = LocalDate.of(2025, 5, 28);
        given(statService.getDailySalesSummary(eq(testDate))).willReturn(null);

        ResultActions actions = mockMvc.perform(get("/api/admin/stats/daily-summary").param("date", testDate.toString()));

        actions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("해당 날짜의 판매 요약 데이터가 없습니다."));
    }
}