package com.realive.service.seller;

import org.springframework.web.multipart.MultipartFile;

import com.realive.domain.seller.Seller;
import com.realive.dto.seller.SellerLoginRequestDTO;
import com.realive.dto.seller.SellerLoginResponseDTO;
import com.realive.dto.seller.SellerResponseDTO;
import com.realive.dto.seller.SellerSignupDTO;
import com.realive.dto.seller.SellerUpdateDTO;

public interface SellerService {

    SellerLoginResponseDTO login(SellerLoginRequestDTO reqdto);

    SellerResponseDTO getMyInfo(Long sellerId);

    void registerSeller(SellerSignupDTO dto, MultipartFile businessLicense, MultipartFile bankAccountCopy);

    void updateSeller(Long sellerId, SellerUpdateDTO dto);

    Seller getByEmail(String email);
}
