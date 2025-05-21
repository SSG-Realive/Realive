package com.realive.repository.seller;

import com.realive.domain.seller.SellerQna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerQnaRepository extends JpaRepository<SellerQna, Long> {

    /**
     * 특정 판매자(sellerId)가 받은 QnA 목록을 페이징으로 조회
     */
    Page<SellerQna> findBySellerId(Long sellerId, Pageable pageable);
}