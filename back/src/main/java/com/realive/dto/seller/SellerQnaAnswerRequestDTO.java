package com.realive.dto.seller;

import lombok.*;

/**
 * 판매자 QnA 답변 등록 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerQnaAnswerRequestDTO {

    private String answer; // 작성할 답변

}