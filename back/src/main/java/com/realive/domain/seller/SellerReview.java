package com.realive.domain.seller;

import com.realive.domain.common.BaseTimeEntity;

import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
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
 * 판매자 리뷰(SellerReview) 엔티티
 * - 특정 주문(order_id)에 대해 하나의 리뷰만 작성 가능하도록 unique 제약 설정
 * - 리뷰는 특정 판매자(seller)에게 작성됨
 * - BaseTimeEntity를 상속하여 생성/수정 시간 자동 관리
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "seller_reviews",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"order_id"}) // 주문당 리뷰 하나만 작성 가능
        })
public class SellerReview extends BaseTimeEntity {

    /** 리뷰 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 리뷰가 연결된 주문 (미사용 중)
     * 향후 연결 필요 시 주석 해제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * 리뷰 작성한 고객 (미사용 중)
     * 향후 연결 필요 시 주석 해제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // 리뷰 대상 판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // 평점 (예: 1 ~ 5)
    @Column(nullable = false)
    private int rating;

    // 리뷰 본문 내용
    @Column(nullable = false, length = 1000)
    private String content;

    /**
     * 리뷰의 숨김/공개 상태입니다. (true: 숨김, false: 공개)
     * - @Column(name = "is_hidden", nullable = false):
     *   DB의 'is_hidden' 컬럼과 매핑되며, NOT NULL 제약이 추가됩니다.
     *   DB 스키마에도 'DEFAULT false' 설정을 권장합니다.
     * - @Builder.Default:
     *   Lombok @Builder 사용 시, 이 필드 값을 명시적으로 설정하지 않으면
     *   아래 초기값(Boolean.FALSE)이 기본값으로 사용되도록 보장합니다.
     *   이것이 없으면, 빌더 사용 시 isHidden 필드가 null이 되어
     *   @Column(nullable = false) 제약과 충돌할 수 있습니다.
     */
    @Column(name = "is_hidden", nullable = false) // DB 컬럼명 및 NOT NULL 제약
    @Builder.Default // Lombok 빌더 사용 시 기본값 적용
    private Boolean isHidden = Boolean.FALSE;   // Java 객체 기본값 false (공개)
}