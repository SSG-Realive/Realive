package com.realive.repository.auction;

import com.realive.domain.auction.AdminProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List; // List import 추가
import java.util.Optional;

public interface AdminProductRepository extends JpaRepository<AdminProduct, Integer> {

    Optional<AdminProduct> findById(Integer id);

    Optional<AdminProduct> findByProductId(Integer productId);

    List<AdminProduct> findByProductIdIn(List<Integer> productIds); // 이전에 추가한 메소드

    // --- 아래 메소드를 추가합니다 ---
    /**
     * 특정 판매자 ID(purchasedFromSellerId)를 가진 모든 AdminProduct 목록을 조회합니다.
     * AuctionServiceImpl의 getAuctionsBySeller 메소드에서 사용됩니다.
     * @param sellerId AdminProduct의 purchasedFromSellerId (타입 주의 - 여기서는 Integer로 가정)
     * @return 해당 판매자가 구매한 AdminProduct 목록
     */
    List<AdminProduct> findByPurchasedFromSellerId(Integer sellerId);
    // 만약 페이징이 필요하다면:
    // Page<AdminProduct> findByPurchasedFromSellerId(Integer sellerId, Pageable pageable);
    // --- 여기까지 추가 ---

    @Query("select ap.id, ap.productId, ap.purchasedFromSellerId, ap.purchasedAt, " +
            "  ap.isAuctioned " +
            " from AdminProduct ap ")
    Page<Object[]> list1(Pageable pageable);
}
