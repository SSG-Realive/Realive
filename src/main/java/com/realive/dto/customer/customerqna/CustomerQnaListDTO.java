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
    private LocalDateTime createdAt;    
    private Boolean isAnswered;         

}
