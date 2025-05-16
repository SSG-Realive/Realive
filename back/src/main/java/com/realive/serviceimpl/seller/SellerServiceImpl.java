package com.realive.serviceimpl.seller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.realive.domain.seller.Seller;
import com.realive.dto.seller.SellerLoginRequestDTO;
import com.realive.dto.seller.SellerLoginResponseDTO;
import com.realive.repository.seller.SellerRepository;
import com.realive.security.JwtUtil;
import com.realive.service.seller.SellerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService{

    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public SellerLoginResponseDTO login(SellerLoginRequestDTO reqdto){
       
        // email로 사용자 찾기
        Seller seller = sellerRepository.findByEmail(reqdto.getEmail())
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 이메일입니다."));
            
                // 비밀번호 확인
            if (!passwordEncoder.matches(reqdto.getPassword(), seller.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

            } 
            // accesstoken, refreshtoken 생성
            String accessToken = jwtUtil.generateAccessToken(seller);
            String refreshToken = jwtUtil.generateAccessToken(seller);

            // dto 반환
            return SellerLoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .email(seller.getEmail())
                    .name(seller.getName())
                    .build();
         }
    
}
