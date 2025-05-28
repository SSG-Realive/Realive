package com.realive.dto.seller;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SellerQnaListDTO {

    private Long id;                // QnA ID
    private String title;           // 질문 제목
    private boolean isAnswered;     // 답변 여부
    private LocalDateTime createdAt; // 질문 등록일
    private LocalDateTime answeredAt; // 답변일 (있다면)

    public static SellerQnaListDTO fromEntity(com.realive.domain.seller.SellerQna qna) {
        return SellerQnaListDTO.builder()
                .id(qna.getId())
                .title(qna.getTitle())
                .isAnswered(qna.isAnswered())
                .createdAt(qna.getCreatedAt())
                .answeredAt(qna.getAnsweredAt())
                .build();
    }
}