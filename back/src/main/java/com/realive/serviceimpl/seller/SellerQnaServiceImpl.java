package com.realive.serviceimpl.seller;

import com.realive.domain.seller.SellerQna;
import com.realive.dto.seller.SellerQnaAnswerRequestDTO;
import com.realive.dto.seller.SellerQnaResponseDTO;
import com.realive.repository.seller.SellerQnaRepository;
import com.realive.service.seller.SellerQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 판매자 QnA 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class SellerQnaServiceImpl implements SellerQnaService {

    private final SellerQnaRepository qnaRepository;

    /**
     * 판매자의 QnA 목록을 페이징으로 조회합니다.
     */
    @Override
    public Page<SellerQnaResponseDTO> getQnaListBySeller(Long sellerId, Pageable pageable) {
        return qnaRepository.findBySellerId(sellerId, pageable)
                .map(qna -> SellerQnaResponseDTO.builder()
                        .id(qna.getId())
                        .title(qna.getTitle())
                        .content(qna.getContent())
                        .answer(qna.getAnswer())
                        .isAnswered(qna.isAnswered())
                        .createdAt(qna.getCreatedAt())
                        .updatedAt(qna.getUpdatedAt())
                        .answeredAt(qna.getAnsweredAt())
                        .build());
    }

    /**
     * QnA에 대해 판매자가 답변을 등록하거나 수정합니다.
     * 권한이 없는 경우 예외 발생.
     */
    @Override
    public void answerQna(Long sellerId, Long qnaId, SellerQnaAnswerRequestDTO dto) {
        SellerQna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("QnA not found"));

        // 자신의 QnA가 아닌 경우 예외
        if (!qna.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("자신의 QnA에만 답변할 수 있습니다.");
        }

        // 답변 등록/수정
        qna.setAnswer(dto.getAnswer());
        qna.setAnswered(true);
        qna.setAnsweredAt(LocalDateTime.now());

        qnaRepository.save(qna);
    }
}