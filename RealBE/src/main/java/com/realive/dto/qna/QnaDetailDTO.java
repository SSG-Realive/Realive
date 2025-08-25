package com.realive.dto.qna;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// QnA 상세 조회를 위한 DTO

@Data
@Builder
public class QnaDetailDTO {
    private Long id;                // QnA ID
    private String title;          // 질문 제목
    private String content;        // 질문 내용
    private String answer;         // 답변 내용 (nullable)
    private boolean isAnswered;    // 답변 여부
    private LocalDateTime createdAt;   // 질문 작성일
    private LocalDateTime updatedAt;   // 마지막 수정일=판매자 답변 작성일
    private LocalDateTime answeredAt;  // 답변 작성일 (nullable)
}
