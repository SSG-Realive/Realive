package com.realive.serviceimpl.seller;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.realive.domain.common.enums.SellerFileType;
import com.realive.domain.seller.Seller;
import com.realive.domain.seller.SellerDocument;
import com.realive.dto.seller.SellerLoginRequestDTO;
import com.realive.dto.seller.SellerLoginResponseDTO;
import com.realive.dto.seller.SellerResponseDTO;
import com.realive.dto.seller.SellerSignupDTO;
import com.realive.dto.seller.SellerUpdateDTO;
import com.realive.repository.seller.SellerDocumentRepository;
import com.realive.repository.seller.SellerRepository;
import com.realive.security.JwtUtil;
import com.realive.security.seller.SellerPrincipal;
import com.realive.service.common.FileUploadService;
import com.realive.service.seller.SellerService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SellerDocumentRepository sellerDocumentRepository;
    private final FileUploadService fileUploadService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public Seller getByEmail(String email) {
        return sellerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
    }

    @Override
    @Transactional
    public SellerLoginResponseDTO login(SellerLoginRequestDTO request) {
        // 1. 사용자가 보낸 이메일/비밀번호로 인증 토큰 생성
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(request.getEmail(),
                request.getPassword());

        // 2. AuthenticationManager에게 인증 위임 -> SellerDetailsService가 실행됨
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 3. 인증 성공! Principal 객체를 가져옴
        SellerPrincipal principal = (SellerPrincipal) authentication.getPrincipal();

        // 4. 승인된 판매자인지 추가 확인
        if (!principal.isApproved()) {
            throw new BadCredentialsException("아직 승인되지 않은 판매자 계정입니다.");
        }

        Long sellerId = principal.getId();
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new UsernameNotFoundException("인증은 성공했으나, DB에서 판매자 정보를 찾을 수 없습니다. ID: " + sellerId));

        // 5. JWT 생성
        String accessToken = jwtUtil.generateAccessToken(principal);
        String refreshToken = jwtUtil.generateRefreshToken(principal);

        seller.setRefreshToken(refreshToken);
        sellerRepository.save(seller);

        // 6. 최종 응답 DTO 생성 및 반환
        return SellerLoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(principal.getUsername())
                .name(principal.getName())
                .build();
    }

    // 판매자 정보 조회
    @Override
    public SellerResponseDTO getMyInfo(Seller seller) {

        // 판매자 정보 생성
        return SellerResponseDTO.builder()
                .email(seller.getEmail())
                .name(seller.getName())
                .phone(seller.getPhone())
                .isApproved(seller.isApproved())
                .businessNumber(seller.getBusinessNumber())
                .hasBankAccountCopy(true)
                .build();

    }

    // 회원가입
    @Override
    @Transactional
    public Seller registerSeller(SellerSignupDTO dto) {

        // 이메일 존재 유무 검증.
        if (sellerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");

        }
        // 이름 중복 검증.
        if (sellerRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        }
        // 비번 인코딩저장.
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // dto로 전달받은 정보로 seller 객체 생성
        Seller seller = Seller.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .phone(dto.getPhone())
                .password(encodedPassword)
                .businessNumber(dto.getBusinessNumber())
                .isActive(true)
                .isApproved(false)
                .build();
        // dto 받은거 저장.
        return sellerRepository.save(seller);

    }

    // 회원수정
    @Override
    @Transactional
    public void updateSeller(Seller seller, SellerUpdateDTO dto) {
        // 판매자 정보 조회
        seller.setName(dto.getName());
        seller.setPhone(dto.getPhone());
        // 판매자 이름 수정 및 검증
        if (!seller.getName().equals(dto.getName())) {
            if (sellerRepository.existsByName(dto.getName())) {
                throw new IllegalArgumentException("이미 사용중인 이름입니다.");
            }

            seller.setName(dto.getName());

        } // end if
          // 판매자 전화번호 수정
        if (!seller.getPhone().equals(dto.getPhone())) {

            seller.setPhone(dto.getPhone());

        } // end if
          // 판매자 비밀번호 조건부 수정
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {

            String encodedPassword = passwordEncoder.encode(dto.getNewPassword());

            seller.setPassword(encodedPassword);

        } // end if

    }

}
