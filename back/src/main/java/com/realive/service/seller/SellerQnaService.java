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
     * 판매자 기준 QnA 목록 조회 (페이징)
     *
     * @param sellerId 판매자 ID
     * @param pageable 페이지 요청 정보
     * @return QnA 목록 페이지
     */
    Page<SellerQnaResponseDTO> getQnaListBySeller(Long sellerId, Pageable pageable);

    /**
     * QnA 답변 작성 또는 수정
     *
     * @param sellerId 판매자 ID
     * @param qnaId    QnA ID
     * @param dto      답변 내용 DTO
     */
    void answerQna(Long sellerId, Long qnaId, SellerQnaAnswerRequestDTO dto);
}