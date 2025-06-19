package com.realive.repository.auction;

import com.realive.domain.auction.AdminProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminProductRepository extends JpaRepository<AdminProduct, Integer>, JpaSpecificationExecutor<AdminProduct> {

    Optional<AdminProduct> findById(Integer id);

    Optional<AdminProduct> findByProductId(Integer productId);

    List<AdminProduct> findByProductIdIn(List<Integer> productIds);

    /**
     * 특정 판매자 ID(purchasedFromSellerId)를 가진 모든 AdminProduct 목록을 조회합니다.
     * AuctionServiceImpl의 getAuctionsBySeller 메소드에서 사용됩니다.
     * @param sellerId AdminProduct의 purchasedFromSellerId (타입 주의 - 여기서는 Integer로 가정)
     * @return 해당 판매자가 구매한 AdminProduct 목록
     */
    List<AdminProduct> findByPurchasedFromSellerId(Integer sellerId);
    // 만약 페이징이 필요하다면:
    // Page<AdminProduct> findByPurchasedFromSellerId(Integer sellerId, Pageable pageable);

    @Query("select ap.id, ap.productId, ap.purchasedFromSellerId, ap.purchasedAt, " +
            "  ap.isAuctioned " +
            " from AdminProduct ap ")
    Page<Object[]> list1(Pageable pageable); // 이 list1 메소드의 사용처는 AuctionServiceImpl에서 명확하지 않으므로, 필요 없다면 제거 가능
}
