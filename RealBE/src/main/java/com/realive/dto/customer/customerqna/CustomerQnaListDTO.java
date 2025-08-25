package com.realive.dto.customer.customerqna;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// [Customer] 내 Q&A 조회DTO

@Data
@Builder
public class CustomerQnaListDTO {

    private Long id;                 
    private String title;
    private String content;          // 추가: 답변을 위해 내용도 포함
    private LocalDateTime createdAt;    
    private Boolean isAnswered;

    // 판매자용 추가 필드들
    private String customerName;     // 추가: 고객 이름 (판매자가 보기 위해)
    private String productName;      // 추가: 상품 이름 (판매자가 보기 위해)
    private Long productId;          // 추가: 상품 ID (판매자가 보기 위해)


}
