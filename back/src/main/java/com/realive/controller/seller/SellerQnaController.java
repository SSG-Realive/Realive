package com.realive.controller.seller;


import com.realive.dto.seller.SellerQnaAnswerRequestDTO;
import com.realive.dto.seller.SellerQnaResponseDTO;
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

    // QnA 목록 조회
    @GetMapping
    public ResponseEntity<Page<SellerQnaResponseDTO>> getQnaList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Page<SellerQnaResponseDTO> qnaList = sellerQnaService.getQnaListByEmail(email, pageable);
        return ResponseEntity.ok(qnaList);
    }

    // QnA 답변 작성/수정
    @PatchMapping("/{qnaId}/answer")
    public ResponseEntity<Void> answerQna(
            @PathVariable Long qnaId,
            @RequestBody SellerQnaAnswerRequestDTO dto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        sellerQnaService.answerQnaByEmail(email, qnaId, dto);
        return ResponseEntity.ok().build();
    }
}