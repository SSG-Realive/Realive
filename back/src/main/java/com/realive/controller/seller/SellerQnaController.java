package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.seller.SellerQnaAnswerRequestDTO;
import com.realive.dto.seller.SellerQnaResponseDTO;
import com.realive.service.seller.SellerQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/qna")
public class SellerQnaController {

    private final SellerQnaService sellerQnaService;


    // 판매자 QnA 목록 조회 (페이징 포함)
    @GetMapping
    public ResponseEntity<Page<SellerQnaResponseDTO>> getQnaList(
            @AuthenticationPrincipal Seller seller,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long sellerId = seller.getId(); // JWT 필터에서 등록된 Seller 엔티티
        Page<SellerQnaResponseDTO> qnaList = sellerQnaService.getQnaListBySeller(sellerId, pageable);
        return ResponseEntity.ok(qnaList);
    }


    // 판매자 QnA 답변 작성/수정
    @PatchMapping("/{qnaId}/answer")
    public ResponseEntity<Void> answerQna(
            @AuthenticationPrincipal Seller seller,
            @PathVariable Long qnaId,
            @RequestBody SellerQnaAnswerRequestDTO dto) {

        sellerQnaService.answerQna(seller.getId(), qnaId, dto);
        return ResponseEntity.ok().build();
    }
}