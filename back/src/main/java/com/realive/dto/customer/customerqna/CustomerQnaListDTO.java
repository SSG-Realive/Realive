package com.realive.dto.customer.customer_qna;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

// [Customer] 내 Q&A 조회DTO

@Data
@Builder
public class CustomerQnaListDTO {

    private Long id;                 
    private String title;              
    private LocalDateTime createdAt;    
    private Boolean isAnswered;         

}
