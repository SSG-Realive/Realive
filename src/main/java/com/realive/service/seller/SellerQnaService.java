package com.realive.service.seller;

import com.realive.dto.sellerqna.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerQnaService {

    // QnA 작성 (새로 추가)
    void createQna(Long sellerId, SellerQnaRequestDTO dto);

    // QnA 목록 조회
    Page<SellerQnaResponseDTO> getQnaListBySellerId(Long sellerId, Pageable pageable);

    // QnA 단건 조회
    SellerQnaDetailResponseDTO getQnaDetail(Long sellerId, Long qnaId);

    // QnA 삭제 (soft delete)
    void deleteQna(Long sellerId, Long qnaId);

    // QnA 수정 (답변 없을 때만 가능)
    void updateQnaContent(Long sellerId, Long qnaId, SellerQnaUpdateRequestDTO dto);

    // QnA 답변(고객이 작성한 QnA)
    void answerQna(Long sellerId, Long qnaId, String answer);

}