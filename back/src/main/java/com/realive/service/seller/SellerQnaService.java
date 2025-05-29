package com.realive.service.seller;

import com.realive.dto.sellerqna.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerQnaService {

    Page<SellerQnaResponseDTO> getQnaListBySellerId(Long sellerId, Pageable pageable);

    SellerQnaDetailResponseDTO getQnaDetail(Long sellerId, Long qnaId);

    void deleteQna(Long sellerId, Long qnaId);

    void updateQnaContent(Long sellerId, Long qnaId, SellerQnaUpdateRequestDTO dto);

    void answerQna(Long sellerId, Long qnaId, SellerQnaAnswerRequestDTO dto);
}