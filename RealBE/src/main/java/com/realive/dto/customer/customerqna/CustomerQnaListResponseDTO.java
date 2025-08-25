package com.realive.dto.customer.customerqna;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// [고객용] Q&A 목록 조회 응답 DTO
// 새로 만들지 않으면 다른 기능들이 제대로 작동 하지 않을것으로 예상돼 새로 만들게 됨
@Data
@Builder
public class CustomerQnaListResponseDTO {

    private Long id;                 // Q&A ID
    private String title;            // Q&A 제목
    private String content;          // Q&A 내용
    private String answer;           // Q&A 답변
    private LocalDateTime createdAt; // 질문 생성일
    private Boolean isAnswered;      // 답변 여부

    // 필요하다면, 여기에 고객 이름, 상품 이름 등을 추가할 수 있습니다.
    // private String customerName;
    // private String productName;
    // private Long productId;
}