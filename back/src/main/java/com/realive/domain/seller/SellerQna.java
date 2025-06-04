package com.realive.domain.seller;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.customer.Customer;
import com.realive.domain.product.Product;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 판매자 QnA 엔티티
 * - 판매자 전용 QnA 관리 (질문/답변)
 * - 고객이 질문하고, 판매자가 답변함
 */
@Entity
@Table(name = "seller_qna")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerQna extends BaseTimeEntity {

    // QnA 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 질문이 달린 판매자 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // 질문 제목
    @Column(length = 100, nullable = false)
    private String title;

    // 질문 본문
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 판매자 답변 (nullable)
    @Column(columnDefinition = "TEXT")
    private String answer;

    // 답변 여부 (true: 답변 완료)
    private boolean isAnswered = false;

    // 답변 작성 시간 (있을 경우)
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "is_active")
    private boolean isActive = true;
    // 문의 작성 시간
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // public Boolean getIsAnswered() {
    //     return isAnswered;
    // }

    // 아래처럼 boolean getter는 isXxx()로 써야 함
    public boolean isAnswered() {
        return isAnswered;
    }


}