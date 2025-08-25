package com.realive.service.admin.user;

import com.realive.dto.sellerqna.SellerQnaDetailResponseDTO;
import com.realive.dto.sellerqna.SellerQnaResponseDTO;
import com.realive.dto.sellerqna.SellerQnaStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminQnaService {
    // ✅ 하나의 메서드만 유지 (검색/필터 포함)
    Page<SellerQnaResponseDTO> getAllSellerQnaList(String search, Boolean isAnswered, Pageable pageable);

    // 나머지 메서드들은 동일...
    SellerQnaDetailResponseDTO getSellerQnaDetail(Long qnaId);
    void answerSellerQna(Long qnaId, String answer);
    void deleteSellerQna(Long qnaId);
    SellerQnaStatisticsDTO getSellerQnaStatistics();
}