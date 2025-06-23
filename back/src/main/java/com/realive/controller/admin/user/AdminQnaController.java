package com.realive.controller.admin.user;

import com.realive.dto.customer.customerqna.CustomerQnaDetailDTO;
import com.realive.dto.customer.customerqna.CustomerQnaListDTO;
import com.realive.service.admin.user.AdminQnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/admin/qna") // 관리자 Q&A 관련 API 엔드포인트
@RequiredArgsConstructor
public class AdminQnaController {

    private final AdminQnaService adminQnaService;

    // 전체 고객 Q&A 목록 조회 (페이징 포함)
    @GetMapping("/customer")
    public ResponseEntity<Page<CustomerQnaListDTO>> getAllCustomerQnaList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CustomerQnaListDTO> qnaList = adminQnaService.getAllCustomerQnaList(pageable);
        return ResponseEntity.ok(qnaList);
    }

    // 특정 고객 Q&A 상세 조회
    @GetMapping("/customer/{qnaId}")
    public ResponseEntity<CustomerQnaDetailDTO> getCustomerQnaDetail(@PathVariable Long qnaId) {
        CustomerQnaDetailDTO detail = adminQnaService.getCustomerQnaDetail(qnaId);
        return ResponseEntity.ok(detail);
    }

    // [옵션] 답변되지 않은 고객 Q&A 목록 조회
    @GetMapping("/customer/unanswered")
    public ResponseEntity<Page<CustomerQnaListDTO>> getUnansweredCustomerQnaList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CustomerQnaListDTO> qnaList = adminQnaService.getUnansweredCustomerQnaList(pageable);
        return ResponseEntity.ok(qnaList);
    }

    // [옵션] 특정 상품에 대한 고객 Q&A 목록 조회 (상품 ID로 필터링)
    @GetMapping("/customer/product/{productId}")
    public ResponseEntity<List<CustomerQnaListDTO>> getCustomerQnaListByProduct(@PathVariable Long productId) {
        List<CustomerQnaListDTO> qnaList = adminQnaService.getCustomerQnaListByProduct(productId);
        return ResponseEntity.ok(qnaList);
    }

    // 특정 고객 Q&A 삭제
    @DeleteMapping("/customer/{qnaId}")
    public ResponseEntity<Void> deleteCustomerQna(@PathVariable Long qnaId) {
        adminQnaService.deleteCustomerQna(qnaId);
        return ResponseEntity.noContent().build();
    }
}