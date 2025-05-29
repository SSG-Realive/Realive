package com.realive.dto.customer.customer_qna;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "제목은 비어 있을 수 없습니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 입력할 수 있습니다.")
    private String title;

    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    @Size(max = 1000, message = "제목은 최대 1000자까지 입력할 수 있습니다.")
    private String content;

    private Long productId;
    
    private Long customerId;

}

