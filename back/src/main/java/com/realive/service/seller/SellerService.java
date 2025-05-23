package com.realive.service.seller;

import org.springframework.web.multipart.MultipartFile;

import com.realive.domain.seller.Seller;
import com.realive.dto.seller.SellerLoginRequestDTO;
import com.realive.dto.seller.SellerLoginResponseDTO;
import com.realive.dto.seller.SellerResponseDTO;
import com.realive.dto.seller.SellerSignupDTO;
import com.realive.dto.seller.SellerUpdateDTO;

/**
 * SellerService
 * - 판매자 관련 기능을 정의한 서비스 인터페이스
 * - 인증, 정보 조회 및 수정, 회원가입 등을 처리
 */
public interface SellerService {

    /**
     * 판매자 로그인 처리
     *
     * @param reqdto 로그인 요청 DTO (이메일, 비밀번호)
     * @return 로그인 성공 시 JWT 토큰 등 포함된 응답 DTO
     */
    SellerLoginResponseDTO login(SellerLoginRequestDTO reqdto);

    /**
     * 내 정보 조회
     *
     * @param sellerId 로그인된 판매자의 ID
     * @return 판매자 정보 DTO
     */
    SellerResponseDTO getMyInfo(String email);

    /**
     * 판매자 회원가입 처리
     *
     * @param dto 판매자 회원가입 정보 DTO
     * @param businessLicense 사업자등록증 파일
     * @param bankAccountCopy 통장 사본 파일
     */
    void registerSeller(SellerSignupDTO dto, MultipartFile businessLicense, MultipartFile bankAccountCopy);

    /**
     * 판매자 정보 수정 (이름, 전화번호, 비밀번호 등)
     *
     * @param sellerId 판매자 ID
     * @param dto 수정 요청 DTO
     */
    void updateSeller(String email, SellerUpdateDTO dto);

    /**
     * 이메일로 판매자 조회
     *
     * @param email 판매자 이메일
     * @return 판매자 엔티티
     */
    Seller getByEmail(String email);
}
