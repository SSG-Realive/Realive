package com.realive.dto.sellerqna;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 판매자 QnA 단건 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerQnaDetailResponseDTO {

    private Long id;                // QnA ID
    private String title;          // 질문 제목
    private String content;        // 질문 내용
    private String answer;         // 답변 내용 (nullable)
    private boolean isAnswered;    // 답변 여부
    private LocalDateTime createdAt;   // 질문 작성일
    private LocalDateTime updatedAt;   // 마지막 수정일
    private LocalDateTime answeredAt;  // 답변 작성일 (nullable)

    private Long sellerId;         // 판매자 ID
    private String sellerName;     // 판매자 이름
    private String sellerEmail;    // 판매자 이메일
}