package com.realive.domain.review;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "review_images")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private SellerReview review;

    private String imageUrl;

    private boolean thumbnail;

    @Column(name = "created at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}