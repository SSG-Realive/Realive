package com.realive.controller.seller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.realive.domain.customer.CustomerQna;
import com.realive.domain.seller.Seller;
import com.realive.dto.seller.CustomerQnaAnswerRequestDTO;
import com.realive.service.customer.CustomerQnaService;
import com.realive.service.seller.SellerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/api/seller/customer-qna")
@RequiredArgsConstructor
public class SellerCustomerQnaController {

    private final CustomerQnaService customerQnaService;
    private final SellerService sellerService;

    // 판매자의 상품에 대한 고객 문의 목록 조회 (전체)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCustomerQnaList(Authentication authentication) {
        String email = authentication.getName();
        Seller seller = sellerService.getByEmail(email);
        Long sellerId = seller.getId();

        List<Map<String, Object>> qnaList = customerQnaService.listSellerProductQna(sellerId);
        return ResponseEntity.ok(qnaList);
    }

    // 판매자의 상품에 대한 고객 문의 목록 조회 (페이징)
    @GetMapping("/page")
    public ResponseEntity<Page<Map<String, Object>>> getCustomerQnaPage(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Seller seller = sellerService.getByEmail(email);
        Long sellerId = seller.getId();

        Page<Map<String, Object>> qnaPage = customerQnaService.getSellerQnaPageWithProductSummary(pageable, sellerId);
        return ResponseEntity.ok(qnaPage);
    }

    // 판매자의 답변되지 않은 고객 문의 목록 조회 (페이징)
    @GetMapping("/unanswered/page")
    public ResponseEntity<Page<CustomerQna>> getUnansweredQnaPage(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Seller seller = sellerService.getByEmail(email);
        Long sellerId = seller.getId();

        Page<CustomerQna> qnaPage = customerQnaService.getSellerUnansweredQnaPage(pageable, sellerId);
        return ResponseEntity.ok(qnaPage);
    }

    // 판매자가 고객 문의 상세 조회
    @GetMapping("/{qnaId}")
    public ResponseEntity<Map<String, Object>> getCustomerQnaDetail(
            @PathVariable("qnaId") Long qnaId,
            Authentication authentication) {

        String email = authentication.getName();
        Seller seller = sellerService.getByEmail(email);
        Long sellerId = seller.getId();

        Map<String, Object> detail = customerQnaService.detailSellerProductQna(qnaId, sellerId);
        return ResponseEntity.ok(detail);
    }

    // 판매자가 고객 문의에 답변하기
    @PostMapping("/{qnaId}/answer")
    public ResponseEntity<?> answerCustomerQna(
            @PathVariable("qnaId") Long qnaId,
            @RequestBody @Valid CustomerQnaAnswerRequestDTO requestDTO,
            Authentication authentication) {

        String email = authentication.getName();
        Seller seller = sellerService.getByEmail(email);
        Long sellerId = seller.getId();

        customerQnaService.answerCustomerQna(qnaId, sellerId, requestDTO.getAnswer());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "답변이 등록되었습니다."));
    }
}