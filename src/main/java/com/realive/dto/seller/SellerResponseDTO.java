package com.realive.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SellerResponseDTO
 * - 판매자 정보 응답용 DTO
 * - 마이페이지, 정보 조회 등에서 클라이언트로 전달되는 판매자 정보
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponseDTO {

    private String email;                   // 판매자의 이메일 (고유 로그인 ID)
    private String name;                    // 판매자의 이름 또는 업체명
    private String phone;                   // 판매자의 전화번호
    private boolean isApproved;             // 승인 여부 (관리자에 의해 인증 승인 여부)
    private String businessNumber;          // 사업자 등록 번호
    private boolean hasBankAccountCopy;     // 통장 사본 등록 여부 (판매자 인증 서류 제출 여부 체크)

}