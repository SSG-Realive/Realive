package com.realive.domain.review;

import com.realive.domain.common.BaseTimeEntity; // BaseTimeEntity 임포트
import com.realive.domain.seller.SellerReview;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "review_images")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerReviewImage extends BaseTimeEntity { // BaseTimeEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false) // nullable = false 추가 권장
    private SellerReview review;

    @Column(nullable = false) // 필수값으로 설정
    private String imageUrl;

    @Column(nullable = false) // 필수값으로 설정
    private boolean thumbnail;

    // BaseTimeEntity 상속으로 createdAt, updatedAt 필드 제거
}