package com.realive.dto.sellerqna;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerQnaUpdateRequestDTO {

    private String title;
    private String content;
}