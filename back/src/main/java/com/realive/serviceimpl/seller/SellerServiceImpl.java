package com.realive.serviceimpl.seller;

import java.util.List;

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
import com.realive.service.common.FileUploadService;
import com.realive.service.seller.SellerService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService{

    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SellerDocumentRepository sellerDocumentRepository;
    private final FileUploadService fileUploadService;

     @Override
    public Seller getByEmail(String email){
        return sellerRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 이메일입니다."));
    }


    //로그인
    @Override
    public SellerLoginResponseDTO login(SellerLoginRequestDTO reqdto){

        // email로 사용자 찾기
        Seller seller = sellerRepository.findByEmailAndIsActiveTrue(reqdto.getEmail())
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(reqdto.getPassword(), seller.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

        }
        // accesstoken, refreshtoken 생성
        String accessToken = jwtUtil.generateAccessToken(seller);
        String refreshToken = jwtUtil.generateRefreshToken(seller);

        // dto 반환
        return SellerLoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(seller.getEmail())
                .name(seller.getName())
                .build();
    }

    // 판매자 정보 조회 
    @Override
    public SellerResponseDTO getMyInfo(String email){

        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 판매자입니다."));

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
    public void registerSeller(SellerSignupDTO dto, MultipartFile businessLicense, MultipartFile bankAcountCopy){
        //이메일 존재 유무 검증.
        if (sellerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");

        }
        //이름 중복 검증.
        if (sellerRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        }
        //비번 인코딩저장.
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        Seller seller = Seller.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .phone(dto.getPhone())
                .password(encodedPassword)
                .businessNumber(dto.getBusinessNumber())
                .isApproved(false)
                .build();
        //dto 받은거 저장.
        sellerRepository.save(seller);

        //파일 업로드 
        String licenseUrl = fileUploadService.upload(businessLicense, "사업자등록증", seller.getId());
        String bankUrl = fileUploadService.upload(bankAcountCopy, "통장사본", seller.getId());

        SellerDocument licenceDoc = SellerDocument.builder()
                .seller(seller)
                .fileType(SellerFileType.사업자등록증)
                .fileUrl(licenseUrl)
                .isVerified(false)
                .build();
        
         SellerDocument bankDoc = SellerDocument.builder()
                .seller(seller)
                .fileType(SellerFileType.통장사본)
                .fileUrl(bankUrl)
                .isVerified(false)
                .build();

        sellerDocumentRepository.saveAll(List.of(licenceDoc,bankDoc));
    
    }   
    //회원수정
    @Override
    @Transactional
    public void updateSeller(String email, SellerUpdateDTO dto) {
        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 판매자입니다."));

        if (!seller.getName().equals(dto.getName())) {
            if(sellerRepository.existsByName(dto.getName())){
                throw new IllegalArgumentException("이미 사용중인 이름입니다.");
            }

            seller.setName(dto.getName());

        }//end if

        if (!seller.getPhone().equals(dto.getPhone())) {

            seller.setPhone(dto.getPhone());

        }//end if

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {

            String encodedPassword = passwordEncoder.encode(dto.getNewPassword());

            seller.setPassword(encodedPassword);

        }//end if

    }

   
  
}

