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
 * 판매자(Seller) 엔티티 클래스
 * - 판매자 정보를 저장하는 DB 테이블과 매핑됨
 * - BaseTimeEntity를 상속받아 생성/수정 일시 자동 관리
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sellers")
public class Seller extends BaseTimeEntity {

    /** 판매자 고유 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 판매자 이름 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 이메일 (유일값) */
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    /** 전화번호 */
    @Column(length = 20)
    private String phone;

    /** 사업자 등록번호 */
    @Column(name = "business_number", length = 50)
    private String businessNumber;

    /** 비밀번호 (출력 제외) */
    @Column(nullable = false, length = 100)
    @ToString.Exclude
    private String password;

    /** 승인 여부 */
    @Column(name = "is_approved", nullable = false)
    private boolean isApproved = false;

    /** 승인 시각 (수정 불가) */
    @Column(name = "approved_at", updatable = false)
    private LocalDateTime approvedAt;

    /** 생성 시각 (BaseTimeEntity에도 존재하지만 중복 정의됨) */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 수정 시각 (BaseTimeEntity에도 존재하지만 중복 정의됨) */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 활동 여부 */
    @Column(name = "is_active")
    private boolean isActive = true;
}