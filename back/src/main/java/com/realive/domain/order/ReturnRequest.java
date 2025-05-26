package com.realive.domain.order;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.ReturnReason;
import com.realive.domain.common.enums.ReturnStatus;
import com.realive.domain.product.Product;
import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "return_requests")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReturnRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnReason returnReason;

    @Column(length = 500)
    private String detailedReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "return_request_images", joinColumns = @JoinColumn(name = "return_request_id"))
    @Column(name = "image_url", length = 255)
    private List<String> imageUrls = new ArrayList<>();

    @Column(length = 1000)
    private String adminMemo; // 이미 존재했던 필드

    private LocalDateTime processedAt;

    private int refundAmount;
    private int returnShippingFee;

    public void updateStatus(ReturnStatus newStatus) {
        this.status = newStatus;
        this.processedAt = LocalDateTime.now();
    }

    public void setRefundAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Refund amount cannot be negative.");
        }
        this.refundAmount = amount;
    }

    public void setDetailedReason(String detailedReason) {
        if (this.returnReason == ReturnReason.OTHER_REASON) {
            this.detailedReason = detailedReason;
        } else {
            this.detailedReason = null;
        }
    }

    public void setReturnShippingFee(int fee) {
        if (fee < 0) {
            throw new IllegalArgumentException("Return shipping fee cannot be negative.");
        }
        this.returnShippingFee = fee;
    }

    /**
     * 관리자 메모를 설정합니다.
     * @param adminMemo 관리자 메모 내용
     */
    public void setAdminMemo(String adminMemo) {
        this.adminMemo = adminMemo;
    }
}