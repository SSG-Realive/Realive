package com.realive.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CustomerQnaRequestDTO {

    private String title;
    private String content;
    private Long productId;
    private Long customerId;

}

