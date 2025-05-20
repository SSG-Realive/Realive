package com.realive.service.seller;

import com.realive.dto.seller.SellerLoginRequestDTO;
import com.realive.dto.seller.SellerLoginResponseDTO;

public interface SellerService {

    SellerLoginResponseDTO login(SellerLoginRequestDTO reqdto);

}
