package com.realive.realive;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.controller.admin.AdminSellerApprovalController;
import com.realive.dto.admin.approval.PendingSellerDTO;
import com.realive.dto.admin.approval.SellerDecisionRequestDTO;
import com.realive.dto.seller.SellerResponseDTO;
import com.realive.service.admin.approval.SellerApprovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime; // SellerResponseDTO에 approvedAt이 있다면 필요
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // POST 테스트 시 CSRF
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminSellerApprovalController.class) // 테스트 대상 컨트롤러 지정
class AdminSellerApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean  // SellerApprovalService를 가짜 객체(Mock)로 대체
    private SellerApprovalService sellerApprovalService;

    @Autowired
    private ObjectMapper objectMapper; // 객체 <-> JSON 변환

    @Autowired
    private WebApplicationContext context;

    // 각 테스트 실행 전 MockMvc에 Spring Security 필터 적용
    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity()) // Spring Security 필터 적용
                .alwaysDo(print())       // 모든 요청/응답 상세 내용 콘솔 출력
                .build();
    }

    // --- 1. 승인 대기 목록 조회 API 테스트 ---
    @Test
    @DisplayName("GET /pending - 성공 (데이터 있음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN") // "ADMIN" 역할로 인증된 사용자 시뮬레이션
    void getPendingApprovalSellers_withData_shouldReturnOk() throws Exception {
        // given: 서비스가 반환할 가짜 데이터 준비
        PendingSellerDTO seller1 = PendingSellerDTO.builder().id(1L).name("판매자1").email("seller1@example.com").businessNumber("111-11-11111").build();
        PendingSellerDTO seller2 = PendingSellerDTO.builder().id(2L).name("판매자2").email("seller2@example.com").businessNumber("222-22-22222").build();
        List<PendingSellerDTO> mockResponse = Arrays.asList(seller1, seller2);

        given(sellerApprovalService.getPendingApprovalSellers()).willReturn(mockResponse);

        // when: API 호출
        ResultActions actions = mockMvc.perform(get("/api/admin/sellers/pending")
                .contentType(MediaType.APPLICATION_JSON));

        // then: 결과 검증
        actions
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("판매자1"));
    }

    @Test
    @DisplayName("GET /pending - 성공 (데이터 없음) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void getPendingApprovalSellers_noData_shouldReturnNoContent() throws Exception {
        // given
        given(sellerApprovalService.getPendingApprovalSellers()).willReturn(Collections.emptyList());

        // when
        ResultActions actions = mockMvc.perform(get("/api/admin/sellers/pending")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        actions
                .andExpect(status().isNoContent()); // 204 No Content
    }

    @Test
    @DisplayName("GET /pending - 일반 사용자 접근 시 403 Forbidden")
    @WithMockUser(roles = "USER") // "USER" 역할 (관리자 아님)
    void getPendingApprovalSellers_asUser_shouldReturnForbidden() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/api/admin/sellers/pending")
                .contentType(MediaType.APPLICATION_JSON));
        // then
        actions
                .andExpect(status().isForbidden()); // 403 Forbidden
    }

    // --- 2. 업체 승인/거부 처리 API 테스트 ---
    @Test
    @DisplayName("POST /approve - 승인 성공 (최초 처리) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void processSellerDecision_approveSuccess_shouldReturnOk() throws Exception {
        // given
        Long sellerId = 1L;
        SellerDecisionRequestDTO requestDto = new SellerDecisionRequestDTO();
        requestDto.setSellerId(sellerId);
        requestDto.setApprove(true);

        SellerResponseDTO mockResponse = SellerResponseDTO.builder()
                .name("판매자1")
                .email("seller1@example.com")
                .isApproved(true) // 서비스에서 isApproved=true로 설정 후 반환 가정
                // .approvedAt(LocalDateTime.now()) // 만약 DTO에 approvedAt이 있고 서비스가 설정한다면
                .businessNumber("111-11-11111")
                .build();

        given(sellerApprovalService.processSellerDecision(sellerId, true, 0)).willReturn(mockResponse);

        // when
        ResultActions actions = mockMvc.perform(post("/api/admin/sellers/approve")
                .with(csrf()) // CSRF 토큰 추가 (Spring Security 사용 시 POST, PUT 등에 필요)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isApproved").value(true))
                .andExpect(jsonPath("$.name").value("판매자1"));

        verify(sellerApprovalService).processSellerDecision(sellerId, true, 0);
    }

    @Test
    @DisplayName("POST /approve - 거부 성공 (최초 처리, isActive=false) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void processSellerDecision_rejectSuccess_shouldReturnOk() throws Exception {
        // given
        Long sellerId = 2L;
        SellerDecisionRequestDTO requestDto = new SellerDecisionRequestDTO();
        requestDto.setSellerId(sellerId);
        requestDto.setApprove(false); // 거부

        SellerResponseDTO mockResponse = SellerResponseDTO.builder()
                .name("판매자2")
                .email("seller2@example.com")
                .isApproved(false) // 서비스에서 isApproved=false로 설정 후 반환 가정
                // .isActive(false) // 만약 DTO에 isActive가 있다면
                .businessNumber("222-22-22222")
                .build();

        given(sellerApprovalService.processSellerDecision(sellerId, false, 0)).willReturn(mockResponse);

        // when
        ResultActions actions = mockMvc.perform(post("/api/admin/sellers/approve")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isApproved").value(false));
        // 만약 서비스에서 isActive=false로 변경하고 DTO에 반영한다면:
        // .andExpect(jsonPath("$.isActive").value(false));

        verify(sellerApprovalService).processSellerDecision(sellerId, false, 0);
    }

    @Test
    @DisplayName("POST /approve - 이미 처리된 판매자 (approvedAt != null) - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void processSellerDecision_alreadyProcessed_shouldReturnConflict() throws Exception {
        // given
        Long sellerId = 3L;
        SellerDecisionRequestDTO requestDto = new SellerDecisionRequestDTO();
        requestDto.setSellerId(sellerId);
        requestDto.setApprove(true); // 승인 시도

        String errorMessage = "이미 승인/거부 처리가 완료된 판매자(ID: " + sellerId + ")입니다. 상태를 변경할 수 없습니다.";
        given(sellerApprovalService.processSellerDecision(sellerId, true, 0))
                .willThrow(new IllegalStateException(errorMessage));

        // when
        ResultActions actions = mockMvc.perform(post("/api/admin/sellers/approve")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        actions
                .andExpect(status().isConflict()) // 409 Conflict
                .andExpect(content().string(errorMessage));
    }

    @Test
    @DisplayName("POST /approve - 존재하지 않는 판매자 - 관리자 권한")
    @WithMockUser(roles = "ADMIN")
    void processSellerDecision_sellerNotFound_shouldReturnNotFound() throws Exception {
        // given
        Long sellerId = 99L;
        SellerDecisionRequestDTO requestDto = new SellerDecisionRequestDTO();
        requestDto.setSellerId(sellerId);
        requestDto.setApprove(true);

        String errorMessage = "ID가 " + sellerId + "인 판매자를 찾을 수 없습니다.";
        given(sellerApprovalService.processSellerDecision(sellerId, true, 0))
                .willThrow(new NoSuchElementException(errorMessage));

        // when
        ResultActions actions = mockMvc.perform(post("/api/admin/sellers/approve")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));
        // then
        actions
                .andExpect(status().isNotFound()) // 404 Not Found
                .andExpect(content().string(errorMessage));
    }

    @Test
    @DisplayName("POST /approve - 일반 사용자 접근 시 403 Forbidden")
    @WithMockUser(roles = "USER")
    void processSellerDecision_asUser_shouldReturnForbidden() throws Exception {
        // given
        Long sellerId = 1L;
        SellerDecisionRequestDTO requestDto = new SellerDecisionRequestDTO();
        requestDto.setSellerId(sellerId);
        requestDto.setApprove(true);

        // when
        ResultActions actions = mockMvc.perform(post("/api/admin/sellers/approve")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));
        // then
        actions
                .andExpect(status().isForbidden()); // 403 Forbidden
    }
}
