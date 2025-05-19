package com.realive.dto.seller;

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

    private String name;
    private String phone;
    private String password;
    

    private String businessLicenseUrl;
    private String bankAccountCopyUrl;


}
