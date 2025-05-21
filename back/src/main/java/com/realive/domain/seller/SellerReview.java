package com.realive.domain.seller;

import java.time.LocalDateTime;

import com.realive.domain.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 판매자 리뷰 엔티티
 * - 판매자에 대한 평점 및 리뷰를 저장
 * - 주문 1건당 리뷰는 1개만 가능 (uniqueConstraints로 order_id 제한 예정)
 * - 고객이 작성하며, 리뷰는 판매자에게 귀속됨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "seller_reviews",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"order_id"})  // 주문 1건당 리뷰 1개 제약
        })
public class SellerReview extends BaseTimeEntity {

    // 고유 ID (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 연관관계 (현재 주석 처리됨, 추후 복원 예정)
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "order_id", nullable = false)
    // private Order order;

    // 리뷰 작성자 (고객) 연관관계 (현재 주석 처리됨, 추후 복원 예정)
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "customer_id", nullable = false)
    // private Customer customer;

    // 리뷰 대상 판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // 평점 (예: 1~5점)
    @Column(nullable = false)
    private int rating;

    // 리뷰 내용
    @Column(nullable = false, length = 1000)
    private String content;

    // 리뷰 생성일
    @Column(name = "created at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // ⚠️ BaseTimeEntity의 createdAt과 중복됨
}