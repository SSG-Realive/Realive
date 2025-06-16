package com.realive.repository.seller;

import com.realive.domain.seller.SellerQna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerQnaRepository extends JpaRepository<SellerQna, Long> {

    /**
     * 삭제되지 않은 QnA만 조회 (isActive = true)
     * - 판매자 마이페이지 등에서 사용
     */
    Page<SellerQna> findBySellerIdAndIsActiveTrue(Long sellerId, Pageable pageable);

    /**
     * 삭제 여부와 관계없이 전체 QnA 조회 (관리자 등)
     * - 필요 시 유지 가능
     */
    Page<SellerQna> findBySellerId(Long sellerId, Pageable pageable);
}