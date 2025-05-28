package com.realive.dto.seller;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 판매자 QnA 상세 조회 DTO
 * - 질문 제목/내용, 답변, 작성일, 답변일 등 상세 정보를 포함
 */
@Getter
@Builder
public class SellerQnaDetailDTO {

    private Long id; // QnA 고유 ID
    private String title; // 질문 제목
    private String content; // 질문 내용
    private String answer; // 판매자 답변
    private boolean isAnswered; // 답변 여부
    private LocalDateTime createdAt; // 질문 작성일
    private LocalDateTime answeredAt; // 답변 작성일 (null 가능)

    /**
     * SellerQna 엔티티를 DTO로 변환
     * @param qna SellerQna 엔티티
     * @return SellerQnaDetailDTO
     */
    public static SellerQnaDetailDTO fromEntity(com.realive.domain.seller.SellerQna qna) {
        return SellerQnaDetailDTO.builder()
                .id(qna.getId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .answer(qna.getAnswer())
                .isAnswered(qna.isAnswered())
                .createdAt(qna.getCreatedAt())
                .answeredAt(qna.getAnsweredAt())
                .build();
    }
}