package com.realive.serviceimpl.seller;

import com.realive.domain.seller.SellerQna;
import com.realive.dto.sellerqna.*;
import com.realive.repository.seller.SellerQnaRepository;
import com.realive.service.seller.SellerQnaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SellerQnaServiceImpl implements SellerQnaService {

    private final SellerQnaRepository sellerQnaRepository;

    @Override
    @Transactional
    public Page<SellerQnaResponseDTO> getQnaListBySellerId(Long sellerId, Pageable pageable) {
        return sellerQnaRepository.findBySellerIdAndIsActiveTrue(sellerId, pageable)
                .map(q -> SellerQnaResponseDTO.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .content(q.getContent())
                        .answer(q.getAnswer())
                        .isAnswered(q.isAnswered())
                        .createdAt(q.getCreatedAt())
                        .updatedAt(q.getUpdatedAt())
                        .answeredAt(q.getAnsweredAt())
                        .build());
    }

    @Override
    @Transactional
    public SellerQnaDetailResponseDTO getQnaDetail(Long sellerId, Long qnaId) {
        SellerQna qna = sellerQnaRepository.findById(qnaId)
                .filter(q -> q.getSeller().getId().equals(sellerId) && !q.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않거나 권한이 없습니다."));

        return SellerQnaDetailResponseDTO.builder()
                .id(qna.getId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .answer(qna.getAnswer())
                .isAnswered(qna.isAnswered())
                .createdAt(qna.getCreatedAt())
                .updatedAt(qna.getUpdatedAt())
                .answeredAt(qna.getAnsweredAt())
                .sellerId(qna.getSeller().getId())
                .sellerName(qna.getSeller().getName())
                .sellerEmail(qna.getSeller().getEmail())
                .build();
    }

    @Override
    @Transactional
    public void answerQna(Long sellerId, Long qnaId, SellerQnaAnswerRequestDTO dto) {
        SellerQna qna = sellerQnaRepository.findById(qnaId)
                .filter(q -> q.getSeller().getId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않거나 권한이 없습니다."));

        qna.setAnswer(dto.getAnswer());
        qna.setAnswered(true);
        qna.setAnsweredAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteQna(Long sellerId, Long qnaId) {
        SellerQna qna = sellerQnaRepository.findById(qnaId)
                .filter(q -> q.getSeller().getId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("삭제할 권한이 없습니다."));

        qna.setDeleted(true);
        qna.setActive(false);
    }

    @Override
    @Transactional
    public void updateQnaContent(Long sellerId, Long qnaId, SellerQnaUpdateRequestDTO dto) {
        SellerQna qna = sellerQnaRepository.findById(qnaId)
                .filter(q -> q.getSeller().getId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않거나 권한이 없습니다."));

        qna.setTitle(dto.getTitle());
        qna.setContent(dto.getContent());
    }
}