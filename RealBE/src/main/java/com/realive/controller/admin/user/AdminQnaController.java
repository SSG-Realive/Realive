package com.realive.controller.admin.user;

import com.realive.dto.sellerqna.SellerQnaAnswerRequestDTO;
import com.realive.dto.sellerqna.SellerQnaDetailResponseDTO;
import com.realive.dto.sellerqna.SellerQnaResponseDTO;
import com.realive.dto.sellerqna.SellerQnaStatisticsDTO;
import com.realive.service.admin.user.AdminQnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/api/admin/seller-qna") // 경로 변경
@RequiredArgsConstructor
public class AdminQnaController {

    private final AdminQnaService adminQnaService; // AdminQnaService 주입

    // 1. 모든 판매자의 Q&A 목록 조회 (페이징)
    @GetMapping
    public ResponseEntity<Page<SellerQnaResponseDTO>> getAllSellerQnaList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isAnswered,  // ← status 제거
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("관리자 Q&A 목록 조회 - 검색어: {}, 답변상태: {}", search, isAnswered);

        Page<SellerQnaResponseDTO> qnaList = adminQnaService.getAllSellerQnaList(search, isAnswered, pageable);
        return ResponseEntity.ok(qnaList);
    }

    // 2. 특정 판매자 Q&A 상세 조회
    @GetMapping("/{qnaId}")
    public ResponseEntity<SellerQnaDetailResponseDTO> getSellerQnaDetail(@PathVariable Long qnaId) {
        SellerQnaDetailResponseDTO detail = adminQnaService.getSellerQnaDetail(qnaId);
        return ResponseEntity.ok(detail);
    }

    // 3. 관리자가 판매자 Q&A에 답변 등록/수정
    @PostMapping("/{qnaId}/answer")
    public ResponseEntity<Void> answerSellerQna(
            @PathVariable Long qnaId,
            @RequestBody SellerQnaAnswerRequestDTO answerRequest) {

        adminQnaService.answerSellerQna(qnaId, answerRequest.getAnswer());
        return ResponseEntity.ok().build();
    }

    // 4. 판매자 Q&A 삭제
    @DeleteMapping("/{qnaId}")
    public ResponseEntity<Void> deleteSellerQna(@PathVariable Long qnaId) {
        adminQnaService.deleteSellerQna(qnaId);
        return ResponseEntity.noContent().build();
    }

    // 5. 통계 조회
    @GetMapping("/statistics")
    public ResponseEntity<SellerQnaStatisticsDTO> getSellerQnaStatistics() {
        SellerQnaStatisticsDTO statistics = adminQnaService.getSellerQnaStatistics();
        return ResponseEntity.ok(statistics);
    }

}