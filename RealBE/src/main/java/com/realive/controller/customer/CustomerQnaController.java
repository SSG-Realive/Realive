package com.realive.controller.customer;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.realive.dto.customer.customerqna.CustomerQnaRequestDTO;
import com.realive.service.customer.CustomerQnaService;
import com.realive.service.customer.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// [Customer] Q&A 관련 컨트롤러

@Log4j2
@RestController
@RequestMapping("/api/customer/qna")
@RequiredArgsConstructor
public class CustomerQnaController{

    private final CustomerQnaService customerQnaService;
    private final CustomerService customerService;

    // 고객 상품 문의하기 (상품 요약 정보 포함)
    @PostMapping
    public ResponseEntity<?> customerQna(Authentication authentication,
                                         @RequestBody @Valid CustomerQnaRequestDTO requestDTO) {
        String email = authentication.getName();
        Long customerId = customerService.findIdByEmail(email);
        requestDTO.setCustomerId(customerId);
        Map<String, Object> result = customerQnaService.createQnaWithProductSummary(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 고객 내 Q&A 목록 조회 (상품 요약 정보 포함)
    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyQnaList(Authentication authentication) {

        String email = authentication.getName();
        Long customerId = customerService.findIdByEmail(email);

        List<Map<String, Object>> qnaListWithProduct = customerQnaService.listQnaWithProductSummary(customerId);

        return ResponseEntity.ok(qnaListWithProduct);
    }

    // 고객 내 Q&A 상세 조회 (상품 요약 정보 포함)
    @GetMapping("my/{qnaId}")
    public ResponseEntity<Map<String, Object>> getQnaDetail(@PathVariable("qnaId") Long qnaId, Authentication authentication) {

        String email = authentication.getName();
        Long customerId = customerService.findIdByEmail(email);

        Map<String, Object> detail = customerQnaService.detailQnaWithProductSummary(qnaId, customerId);

        return ResponseEntity.ok(detail);
    }

}