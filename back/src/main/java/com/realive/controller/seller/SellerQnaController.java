package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.sellerqna.*;
import com.realive.security.seller.SellerPrincipal;
import com.realive.service.seller.SellerQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/qna")
public class SellerQnaController {

    private final SellerQnaService sellerQnaService;

    // ✅ QnA 작성
    @PostMapping("/new")
    public ResponseEntity<Void> createQna(@RequestBody SellerQnaRequestDTO dto, @AuthenticationPrincipal SellerPrincipal principal) {
        
        sellerQnaService.createQna(principal.getId(), dto);
        return ResponseEntity.ok().build();
    }

    // ✅ QnA 목록 조회
    @GetMapping
    public ResponseEntity<Page<SellerQnaResponseDTO>> getQnaList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal SellerPrincipal principal) {

        Page<SellerQnaResponseDTO> qnaList = sellerQnaService.getQnaListBySellerId(principal.getId(), pageable);
        return ResponseEntity.ok(qnaList);
    }

    // ✅ QnA 단건 조회
    @GetMapping("/{qnaId}")
    public ResponseEntity<SellerQnaDetailResponseDTO> getQnaDetail(@PathVariable Long qnaId, @AuthenticationPrincipal SellerPrincipal principal) {
        
        SellerQnaDetailResponseDTO detail = sellerQnaService.getQnaDetail(principal.getId(), qnaId);
        return ResponseEntity.ok(detail);
    }

    // ✅ QnA 수정 (답변 전)
    @PutMapping("/{qnaId}/edit")
    public ResponseEntity<Void> updateQna(
            @PathVariable Long qnaId,
            @RequestBody SellerQnaUpdateRequestDTO dto,
            @AuthenticationPrincipal SellerPrincipal principal) {
        
        sellerQnaService.updateQnaContent(principal.getId(), qnaId, dto);
        return ResponseEntity.ok().build();
    }

    // ✅ QnA 삭제 (soft delete)
    @PatchMapping("/{qnaId}/edit")
    public ResponseEntity<Void> deleteQna(@PathVariable Long qnaId, @AuthenticationPrincipal SellerPrincipal principal) {
        
        sellerQnaService.deleteQna(principal.getId(), qnaId);
        return ResponseEntity.ok().build();
    }

    // ✅ QnA 답변 등록 (고객 문의에 대한 답변)
    @PutMapping("/{qnaId}/answer")
    public ResponseEntity<Void> answerToCustomerQna(
            @PathVariable Long qnaId,
            @RequestBody SellerQnaAnswerRequestDTO dto,
            @AuthenticationPrincipal SellerPrincipal principal
    ) {
        
        sellerQnaService.answerQna(principal.getId(), qnaId, dto.getAnswer());
        return ResponseEntity.ok().build();
    }
}