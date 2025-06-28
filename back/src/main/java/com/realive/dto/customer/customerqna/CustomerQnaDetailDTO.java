package com.realive.dto.customer.customerqna;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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

    // 판매자용 추가 필드들
    private String customerName;        // 추가: 고객 이름
    private String productName;         // 추가: 상품 이름
    private Long productId;             // 추가: 상품 ID

}
