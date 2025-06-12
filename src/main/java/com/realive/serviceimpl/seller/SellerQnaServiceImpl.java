package com.realive.serviceimpl.seller;

import com.realive.domain.seller.Seller;
import com.realive.domain.seller.SellerQna;
import com.realive.dto.sellerqna.*;
import com.realive.repository.seller.SellerQnaRepository;
import com.realive.repository.seller.SellerRepository;
import com.realive.service.seller.SellerQnaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerQnaServiceImpl implements SellerQnaService {

    private final SellerQnaRepository sellerQnaRepository;
    private final SellerRepository sellerRepository;

    // ✅ QnA 작성
    @Override
    @Transactional
    public void createQna(Long sellerId, SellerQnaRequestDTO dto) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보가 존재하지 않습니다."));

        SellerQna qna = SellerQna.builder()
                .seller(seller)
                .title(dto.getTitle())
                .content(dto.getContent())
                .isAnswered(false)
                .isActive(true)
                
                .build();

        sellerQnaRepository.save(qna);
    }

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
                .filter(q -> q.getSeller().getId().equals(sellerId) && q.isActive())
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
    public void deleteQna(Long sellerId, Long qnaId) {
        SellerQna qna = sellerQnaRepository.findById(qnaId)
                .filter(q -> q.getSeller().getId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("삭제할 권한이 없습니다."));


        qna.setActive(false);
    }

    @Override
    @Transactional
    public void updateQnaContent(Long sellerId, Long qnaId, SellerQnaUpdateRequestDTO dto) {
        SellerQna qna = sellerQnaRepository.findById(qnaId)
                .filter(q -> q.getSeller().getId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않거나 권한이 없습니다."));

        if (qna.isAnswered()) {
            throw new IllegalStateException("이미 답변된 QnA는 수정할 수 없습니다.");
        }

        qna.setTitle(dto.getTitle());
        qna.setContent(dto.getContent());
    }
}