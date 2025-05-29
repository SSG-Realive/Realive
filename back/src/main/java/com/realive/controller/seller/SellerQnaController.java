package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.sellerqna.SellerQnaAnswerRequestDTO;
import com.realive.dto.sellerqna.SellerQnaDetailResponseDTO;
import com.realive.dto.sellerqna.SellerQnaResponseDTO;
import com.realive.dto.sellerqna.SellerQnaUpdateRequestDTO;
import com.realive.service.seller.SellerQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/qna")
public class SellerQnaController {

    private final SellerQnaService sellerQnaService;

    // ✅ QnA 목록 조회
    @GetMapping
    public ResponseEntity<Page<SellerQnaResponseDTO>> getQnaList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<SellerQnaResponseDTO> qnaList = sellerQnaService.getQnaListBySellerId(seller.getId(), pageable);
        return ResponseEntity.ok(qnaList);
    }

    // ✅ QnA 단건 조회
    @GetMapping("/{qnaId}")
    public ResponseEntity<SellerQnaDetailResponseDTO> getQnaDetail(@PathVariable Long qnaId) {
        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SellerQnaDetailResponseDTO detail = sellerQnaService.getQnaDetail(seller.getId(), qnaId);
        return ResponseEntity.ok(detail);
    }

    // ✅ QnA 답변 작성/수정
    @PatchMapping("/{qnaId}/answer")
    public ResponseEntity<Void> answerQna(
            @PathVariable Long qnaId,
            @RequestBody SellerQnaAnswerRequestDTO dto) {

        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        sellerQnaService.answerQna(seller.getId(), qnaId, dto);
        return ResponseEntity.ok().build();
    }

    // ✅ QnA 삭제
    @DeleteMapping("/{qnaId}")
    public ResponseEntity<Void> deleteQna(@PathVariable Long qnaId) {
        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        sellerQnaService.deleteQna(seller.getId(), qnaId);
        return ResponseEntity.ok().build();
    }

    // ✅ QnA 질문 수정
    @PatchMapping("/{qnaId}")
    public ResponseEntity<Void> updateQna(
            @PathVariable Long qnaId,
            @RequestBody SellerQnaUpdateRequestDTO dto) {

        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        sellerQnaService.updateQnaContent(seller.getId(), qnaId, dto);
        return ResponseEntity.ok().build();
    }
}