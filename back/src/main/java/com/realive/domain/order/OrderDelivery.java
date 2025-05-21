package com.realive.domain.order;

import com.realive.domain.common.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 주문 배송 정보 엔티티
 * - 주문(Order)과 1:1로 연결됨 (uniqueConstraints)
 * - 배송 상태, 시작일, 완료일, 운송장 번호, 택배사 정보 등 관리
 */
@Entity
@Table(name = "order_delivery",
        uniqueConstraints = {@UniqueConstraint(columnNames = "order_id")}) // 한 주문당 하나의 배송 정보만 허용
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDelivery {

    // 배송 고유 ID (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관 주문 (다대일 단방향 매핑)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 배송 상태 (예: PAYMENT, SHIPPING, DELIVERED)
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    // 배송 시작일 (배송 시작 시간)
    @Column(name = "start_date")
    private LocalDateTime startDate;

    // 배송 완료일 (배송 완료 시간)
    @Column(name = "complete_date")
    private LocalDateTime completeDate;

    // 운송장 번호 (택배사 제공)
    @Column(name = "tracking_number")
    private String trackingNumber;

    // 택배사명 (예: CJ대한통운, 한진 등)
    @Column(name = "carrier")
    private String carrier;

    // 생성일 (자동 설정)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 수정일 (자동 설정)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 저장 시 현재 시각으로 생성/수정일 설정
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 업데이트 시 수정일만 갱신
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}