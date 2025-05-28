package com.realive.service.seller;

import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.seller.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 판매자 QnA 서비스 인터페이스
 * - QnA 목록 조회
 * - QnA 답변 등록/수정
 */
public interface SellerQnaService {

    // 1. 판매자 이메일 기준 QnA 목록
    Page<SellerQnaResponseDTO> getQnaListByEmail(String email, Pageable pageable);

    // 2. QnA에 대한 답변 등록 또는 수정
    void answerQnaByEmail(String email, Long qnaId, SellerQnaAnswerRequestDTO dto);

    // ✅ 3. 판매자 ID 기준 QnA 목록 (페이징)
    PageResponseDTO<SellerQnaListDTO> getQnaListBySeller(Long sellerId, int page, int size);

    // ✅ 4. QnA 상세 조회
    SellerQnaDetailDTO getQnaDetail(Long qnaId);

    // ✅ 5. 미답변 QnA 목록 조회
    PageResponseDTO<SellerQnaListDTO> getUnansweredQnaList(Long sellerId, int page, int size);

    // ✅ 6. 질문 수정 (구매자 입장)
    void updateQuestion(Long qnaId, SellerQnaUpdateDTO dto);

    // ✅ 7. 질문 삭제
    void deleteQna(Long qnaId);

    // ✅ 8. 질문 등록
    Long createQna(SellerQnaRequestDTO dto);
}
