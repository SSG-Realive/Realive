package com.realive.repository.auction;

import com.realive.domain.auction.AdminProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

<<<<<<< HEAD
import java.util.List; // List import 추가
=======
import java.util.List;
>>>>>>> 8959463cf51242389515b4c0a5b0de74a469d25e
import java.util.Optional;

public interface AdminProductRepository extends JpaRepository<AdminProduct, Integer> {

    Optional<AdminProduct> findById(Integer id);

    Optional<AdminProduct> findByProductId(Integer productId);

<<<<<<< HEAD
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
=======
    /**
     * 여러 Product ID에 해당하는 AdminProduct 목록을 조회합니다.
     * AuctionServiceImpl의 getActiveAuctions 메소드에서 N+1 문제를 피하기 위해 사용됩니다.
     * @param productIds 조회할 Product ID 목록
     * @return 해당 Product ID들을 가진 AdminProduct 목록
     */
    List<AdminProduct> findByProductIdIn(List<Integer> productIds);

>>>>>>> 8959463cf51242389515b4c0a5b0de74a469d25e

    @Query("select ap.id, ap.productId, ap.purchasedFromSellerId, ap.purchasedAt, " +
            "  ap.isAuctioned " +
            " from AdminProduct ap ")
    Page<Object[]> list1(Pageable pageable);
}
