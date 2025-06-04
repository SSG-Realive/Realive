package com.realive.dto.qna;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QnaListDTO {
    private Long id;                 // 문의 ID
    private String title;               // 문의 제목
    private LocalDateTime createdAt;    // 문의 등록일
    private Boolean isAnswered;         // 답변 여부
}
