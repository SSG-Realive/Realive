package com.realive.dto.customer.customerqna;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

// [Customer] 내 Q&A 상세 조회DTO

@Data
@Builder
public class CustomerQnaDetailDTO {

    private Long id;               
    private String title;          
    private String content;        
    private String answer;              // 답변 내용 (nullable)
    private boolean isAnswered;         // 답변 여부
    private LocalDateTime createdAt;    // 질문 작성일
    private LocalDateTime updatedAt;   
    private LocalDateTime answeredAt;   // 답변 작성일 (nullable)

}
