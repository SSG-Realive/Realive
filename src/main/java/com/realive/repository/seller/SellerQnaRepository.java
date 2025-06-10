package com.realive.repository.seller;

import com.realive.domain.seller.SellerQna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.nio.channels.FileChannel;
import java.util.Optional;

public interface SellerQnaRepository extends JpaRepository<SellerQna, Long> {

    // 일반 목록 조회 (마이페이지 등)
    Page<SellerQna> findBySellerIdAndIsActiveTrue(Long sellerId, Pageable pageable);

    // 관리자 전체 목록 조회
    Page<SellerQna> findBySellerId(Long sellerId, Pageable pageable);

    // 단건 조회 (판매자 본인 확인)
    Optional<SellerQna> findByIdAndSellerIdAndIsActiveTrue(Long id, Long sellerId);

    // 미답변 필터링 조회
    Page<SellerQna> findBySellerIdAndIsAnsweredFalseAndDIsActiveTrue(Long sellerId, Pageable pageable);

    // 미답변 QnA 수
    long countBySellerIdAndIsAnsweredFalseAndIsActiveTrue(Long sellerId);

    // 총 QnA 수
    long countBySellerIdAndIsActiveTrue(Long sellerId);

}