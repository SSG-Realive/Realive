package com.realive.dto.customer.qna;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data

// [Customer] Q&A 요청DTO

public class CustomerQnaRequestDTO {

    private String title;
    private String content;
    private Long productId;
    private Long customerId;

}

