package com.realive.domain.order;

import com.realive.domain.common.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_delivery",
        uniqueConstraints = {@UniqueConstraint(columnNames = "order_id")}) // 1:1 제약
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 단방향 다대일
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus; // PAYMENT, SHIPPING, DELIVERED

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "complete_date")
    private LocalDateTime completeDate;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}