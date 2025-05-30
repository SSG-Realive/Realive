package com.realive.realive;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.realive.config.JpaAuditingConfig;
// @EnableJpaAuditing이 분리된 설정 클래스
import com.realive.controller.auction.AuctionController;
import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.dto.auction.AdminProductDTO;
import com.realive.repository.seller.SellerRepository;
import com.realive.security.JwtUtil;
import com.realive.service.admin.auction.AuctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan; // FilterType 사용을 위해 import
import org.springframework.context.annotation.FilterType;   // FilterType 사용을 위해 import
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// --- 기본적인 DB/JPA 자동 설정 제외를 위한 import ---
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuctionController.class,
        // @EnableJpaAuditing을 포함하는 JpaAuditingConfig 클래스를 필터로 제외
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaAuditingConfig.class)
        },
        // 기본적인 DB 및 JPA 자동 설정은 여전히 제외하는 것이 좋음
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class,
                TransactionAutoConfiguration.class
                // 'JpaAuditingAutoConfiguration.class'는 찾을 수 없으므로 제거
        }
)
@Import(JwtUtil.class)
@TestPropertySource(properties = {
        "jwt.secret=YourTestSecretKeyNeedsToBeLongEnoughForHS256Algorithm",
        "jwt.expiration=60000"
})
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuctionService auctionService;

    @MockBean
    private SellerRepository sellerRepository;

    private AuctionCreateRequestDTO validCreateRequest;
    private AuctionResponseDTO auctionResponse;
    private AdminProductDTO adminProductResponse;
    private Long mockAuthenticatedUserId = 1L;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        adminProductResponse = AdminProductDTO.builder()
                .id(1).productId(101).productName("테스트용 관리자 상품").auctioned(false).build();
        auctionResponse = AuctionResponseDTO.builder()
                .id(1).productId(101).startPrice(10000).currentPrice(10000)
                .startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusDays(7))
                .adminProduct(adminProductResponse).build();
        validCreateRequest = AuctionCreateRequestDTO.builder()
                .productId(101).startPrice(10000).endTime(LocalDateTime.now().plusDays(7)).build();
    }

    @Test
    @DisplayName("경매 등록 - 성공")
    void registerAuction_Success() throws Exception {
        given(auctionService.registerAuction(any(AuctionCreateRequestDTO.class), eq(mockAuthenticatedUserId)))
                .willReturn(auctionResponse);
        ResultActions resultActions = mockMvc.perform(post("/api/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)));
        resultActions.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("경매가 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data.id").value(auctionResponse.getId()));
        verify(auctionService, times(1)).registerAuction(any(AuctionCreateRequestDTO.class), eq(mockAuthenticatedUserId));
    }

    @Test
    @DisplayName("경매 등록 - 실패 (유효하지 않은 요청 데이터 - @Valid)")
    void registerAuction_Fail_InvalidRequestData() throws Exception {
        AuctionCreateRequestDTO invalidRequest = AuctionCreateRequestDTO.builder().startPrice(-100).build();
        mockMvc.perform(post("/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("경매 등록 - 실패 (서비스 로직 예외 - 상품 없음)")
    void registerAuction_Fail_ProductNotFound() throws Exception {
        String errorMessage = "테스트: 관련 리소스를 찾을 수 없음";
        given(auctionService.registerAuction(any(AuctionCreateRequestDTO.class), eq(mockAuthenticatedUserId)))
                .willThrow(new NoSuchElementException(errorMessage));
        mockMvc.perform(post("/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("경매 등록 - 실패 (서비스 로직 예외 - 권한 없음)")
    void registerAuction_Fail_AccessDenied() throws Exception {
        String errorMessage = "테스트: 접근 권한 없음";
        given(auctionService.registerAuction(any(AuctionCreateRequestDTO.class), eq(mockAuthenticatedUserId)))
                .willThrow(new AccessDeniedException(errorMessage));
        mockMvc.perform(post("/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("진행 중인 경매 목록 조회 - 성공 (데이터 있음)")
    void getActiveAuctions_Success_WithData() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "endTime"));
        List<AuctionResponseDTO> auctions = Collections.singletonList(auctionResponse);
        Page<AuctionResponseDTO> auctionPage = new PageImpl<>(auctions, pageable, 1);
        given(auctionService.getActiveAuctions(eq(pageable), any(), any())).willReturn(auctionPage);
        mockMvc.perform(get("/api/auctions")
                        .param("page", "0").param("size", "10").param("sort", "endTime,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(auctionResponse.getId()));
    }

    @Test
    @DisplayName("진행 중인 경매 목록 조회 - 성공 (데이터 없음)")
    void getActiveAuctions_Success_NoData() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuctionResponseDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        given(auctionService.getActiveAuctions(any(Pageable.class), any(), any())).willReturn(emptyPage);
        mockMvc.perform(get("/api/auctions")
                        .param("page", "0").param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("진행 중인 경매가 없습니다."))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("경매 상세 조회 - 성공")
    void getAuctionDetails_Success() throws Exception {
        Integer auctionId = auctionResponse.getId();
        given(auctionService.getAuctionDetails(eq(auctionId))).willReturn(auctionResponse);
        mockMvc.perform(get("/api/auctions/{auctionId}", auctionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(auctionId));
    }

    @Test
    @DisplayName("경매 상세 조회 - 실패 (존재하지 않는 경매 ID)")
    void getAuctionDetails_Fail_NotFound() throws Exception {
        Integer nonExistentAuctionId = 999;
        String errorMessage = "테스트: 경매를 찾을 수 없습니다. ID: " + nonExistentAuctionId;
        given(auctionService.getAuctionDetails(eq(nonExistentAuctionId)))
                .willThrow(new NoSuchElementException(errorMessage));
        mockMvc.perform(get("/api/auctions/{auctionId}", nonExistentAuctionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}
