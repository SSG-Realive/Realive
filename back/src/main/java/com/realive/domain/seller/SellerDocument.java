package com.realive.domain.seller;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.SellerFileType;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 판매자의 서류 (사업자등록증, 통장사본 등)을 저장하는 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "seller_documents")
public class SellerDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

      // 문서를 업로드한 판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;


    // 업로드한 파일 URL (S3나 서버 내 경로)
    @Column(nullable = false, name = "file_url")
    private String fileUrl;

    // 파일 유형 (enum: 사업자등록증, 통장사본)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false , name = "file_type")
    private SellerFileType fileType;

    // 승인 여부 (0: 미승인, 1: 승인됨)
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    // 승인된 시간 (nullable)
    private LocalDateTime verifiedAt;

  

    
}