package com.realive.repository.auction;

import com.realive.domain.auction.AdminProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminProductRepository extends JpaRepository<AdminProduct, Integer> {

    Optional<AdminProduct> findById(Integer id);

    Optional<AdminProduct> findByProductId(Integer productId);

    /**
     * 여러 Product ID에 해당하는 AdminProduct 목록을 조회합니다.
     * AuctionServiceImpl의 getActiveAuctions 메소드에서 N+1 문제를 피하기 위해 사용됩니다.
     * @param productIds 조회할 Product ID 목록
     * @return 해당 Product ID들을 가진 AdminProduct 목록
     */
    List<AdminProduct> findByProductIdIn(List<Integer> productIds);


    @Query("select ap.id, ap.productId, ap.purchasedFromSellerId, ap.purchasedAt, " +
            "  ap.isAuctioned " +
            " from AdminProduct ap ")
    Page<Object[]> list1(Pageable pageable);
}
