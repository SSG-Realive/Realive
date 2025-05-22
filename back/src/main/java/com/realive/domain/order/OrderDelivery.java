package com.realive.domain.order;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 주문 배송 상태 엔티티
 * - 주문 1건당 배송 정보 1건 (1:1)
 * - 배송 상태 및 운송장 정보 관리
 */
@Entity
@Table(name = "order_delivery",
        uniqueConstraints = {@UniqueConstraint(columnNames = "order_id")}) // 1:1 제약 조건
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDelivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 배송 대상 주문
    @ManyToOne(fetch = FetchType.LAZY) // 단방향 다대일
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 배송 상태 (결제완료, 배송중, 배송완료)
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    // 배송 시작일
    @Column(name = "start_date")
    private LocalDateTime startDate;

    // 배송 완료일
    @Column(name = "complete_date")
    private LocalDateTime completeDate;

    // 운송장 번호 (선택)
    @Column(name = "tracking_number")
    private String trackingNumber;

    // 택배사명 (선택)
    @Column(name = "carrier")
    private String carrier;
}