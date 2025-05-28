package com.realive.repository.seller;

import com.realive.domain.seller.SellerQna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerQnaRepository extends JpaRepository<SellerQna, Long> {

    // 판매자별 활성 QnA 목록
    Page<SellerQna> findBySellerIdAndIsActiveTrue(Long sellerId, Pageable pageable);

    // 전체 QnA 목록
    Page<SellerQna> findBySellerId(Long sellerId, Pageable pageable);

    // 개별 QnA 조회 (삭제 안 된 것만)
    Optional<SellerQna> findByIdAndIsActiveTrue(Long id);

    // 미답변 QnA 조회
    Page<SellerQna> findBySellerIdAndIsAnsweredFalseAndIsActiveTrue(Long sellerId, Pageable pageable);

    Page<SellerQna> findByIsAnsweredFalseAndDeletedFalse(boolean deleted);
}