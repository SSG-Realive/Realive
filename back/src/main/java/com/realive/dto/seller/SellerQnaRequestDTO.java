package com.realive.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerQnaRequestDTO {

    @NotNull
    private Long sellerId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
