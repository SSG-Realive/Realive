package com.realive.service.seller;

import com.realive.dto.seller.SellerQnaAnswerRequestDTO;
import com.realive.dto.seller.SellerQnaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 판매자 QnA 서비스 인터페이스
 * - QnA 목록 조회
 * - QnA 답변 등록/수정
 */
public interface SellerQnaService {

    /**
     * 판매자 이메일 기준 QnA 목록 조회
     */
    Page<SellerQnaResponseDTO> getQnaListByEmail(String email, Pageable pageable);

    /**
     * 판매자 이메일 기준 QnA 답변 작성/수정
     */
    void answerQnaByEmail(String email, Long qnaId, SellerQnaAnswerRequestDTO dto);
}