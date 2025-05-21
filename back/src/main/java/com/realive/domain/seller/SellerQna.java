package com.realive.domain.seller;

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
public class SellerQna {

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

    // 질문 작성 시간 (변경 불가)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 마지막 수정 시간 (답변 포함)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 답변 작성 시간 (있을 경우)
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}