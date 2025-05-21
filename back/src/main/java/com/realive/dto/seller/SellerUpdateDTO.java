package com.realive.dto.seller;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerUpdateDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

   private String newPassword;
    

    


}
