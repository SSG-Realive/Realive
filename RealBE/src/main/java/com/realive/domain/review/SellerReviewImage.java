package com.realive.domain.review;

import com.realive.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "review_images")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SellerReviewImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private SellerReview review;

    // DB 컬럼명 image_url 매핑 (임시로 사용 필요 시)
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // is_thumbnail 컬럼 매핑 (nullable 이므로 Boolean)
    @Column(name = "is_thumbnail")
    private Boolean isThumbnail;

    // thumbnail 컬럼 매핑
    @Column(name = "thumbnail", nullable = false)
    private boolean thumbnail;
}
