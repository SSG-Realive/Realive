package com.realive.domain.order;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "order_delivery")
public class OrderDelivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 50)
    private DeliveryStatus status;

    @Column(name = "tracking_number")
    private Long trackingNumber;

    private String carrier;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "complete_date")
    private LocalDateTime completeDate;
}