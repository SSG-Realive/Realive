package com.realive.controller.seller;

import com.realive.dto.sellerqna.*;
import com.realive.repository.seller.SellerQnaRepository;
import com.realive.security.seller.SellerPrincipal;
import com.realive.service.seller.SellerQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/adminqna")
public class SellerAdminQnaController {

    private final SellerQnaService sellerQnaService;
    private final SellerQnaRepository sellerQnaRepository;

    // ✅ QnA 작성
    @PostMapping("/new")
    public ResponseEntity<Void> createQna(@RequestBody SellerQnaRequestDTO dto, @AuthenticationPrincipal SellerPrincipal principal) {

        sellerQnaService.createQna(principal.getId(), dto);
        return ResponseEntity.ok().build();
    }

    // ✅ QnA 목록 조회 (검색 기능 추가)
    @GetMapping
    public ResponseEntity<Page<SellerQnaResponseDTO>> getQnaList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal SellerPrincipal principal) {

        // keyword를 Service에 전달하도록 수정
        Page<SellerQnaResponseDTO> qnaList = sellerQnaService.getQnaListBySellerIdWithKeyword(principal.getId(), pageable, keyword);
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

    // 📁 수정: SellerAdminQnaController.java


    // ✅ 새로운 통계 API 추가
    @GetMapping("/statistics")
    public ResponseEntity<SellerQnaStatisticsDTO> getQnaStatistics(@AuthenticationPrincipal SellerPrincipal principal) {

        // Repository 메서드 활용 (이미 구현되어 있음)
        long totalCount = sellerQnaRepository.countBySellerIdAndIsActiveTrue(principal.getId());
        long unansweredCount = sellerQnaRepository.countBySellerIdAndIsAnsweredFalseAndIsActiveTrue(principal.getId());
        long answeredCount = totalCount - unansweredCount;
        double answerRate = totalCount > 0 ? (double) answeredCount / totalCount * 100 : 0.0;

        SellerQnaStatisticsDTO statistics = SellerQnaStatisticsDTO.builder()
                .totalCount(totalCount)
                .unansweredCount(unansweredCount)
                .answeredCount(answeredCount)
                .answerRate(answerRate)
                .build();

        return ResponseEntity.ok(statistics);
    }
}