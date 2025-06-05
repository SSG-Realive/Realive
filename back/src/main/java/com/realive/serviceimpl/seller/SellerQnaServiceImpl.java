package com.realive.serviceimpl.seller;

import com.realive.domain.seller.Seller;
import com.realive.domain.seller.SellerQna;
import com.realive.dto.seller.SellerQnaAnswerRequestDTO;
import com.realive.dto.seller.SellerQnaResponseDTO;
import com.realive.repository.seller.SellerQnaRepository;
import com.realive.repository.seller.SellerRepository;
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
    private final SellerRepository sellerRepository;

    @Override
    public Page<SellerQnaResponseDTO> getQnaListByEmail(String email, Pageable pageable) {
        Seller seller = sellerRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보가 존재하지 않습니다."));
        return qnaRepository.findBySellerId(seller.getId(), pageable)
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

    @Override
    public void answerQnaByEmail(String email, Long qnaId, SellerQnaAnswerRequestDTO dto) {
        Seller seller = sellerRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보가 존재하지 않습니다."));

        SellerQna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));

        if (!qna.getSeller().getId().equals(seller.getId())) {
            throw new SecurityException("본인의 QnA에만 답변할 수 있습니다.");
        }

        qna.setAnswer(dto.getAnswer());
        qna.setAnswered(true);
        qna.setAnsweredAt(LocalDateTime.now());
        qnaRepository.save(qna);
    }
}