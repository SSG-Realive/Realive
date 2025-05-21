package com.realive.domain.seller;

import java.time.LocalDateTime;

import com.realive.domain.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 판매자(Seller) 엔티티
 * - 판매자 회원 정보를 저장하는 테이블
 * - 업체 회원가입, 인증 상태, 활성 여부 등을 관리
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sellers")
public class Seller extends BaseTimeEntity {

    // 판매자 고유 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 업체명
    @Column(nullable = false, length = 100)
    private String name;

    // 이메일 (로그인 ID 역할), 중복 불가
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    // 전화번호
    @Column(length = 20)
    private String phone;

    // 사업자 등록번호
    @Column(name = "business_number", length = 50)
    private String businessNumber;

    // 비밀번호 (암호화 저장됨)
    @Column(nullable = false, length = 100)
    @ToString.Exclude
    private String password;

    // 관리자 승인 여부 (false: 미승인, true: 승인됨)
    @Column(name = "is_approved", nullable = false)
    private boolean isApproved = false;

    // 승인된 날짜 (한번 설정되면 수정 불가)
    @Column(name = "approved_at", updatable = false)
    private LocalDateTime approvedAt;

    // 생성일
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 수정일
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 판매자 계정 활성 상태 (false: 비활성화, true: 사용 가능)
    @Column(name = "is_active")
    private boolean isActive = true;
}