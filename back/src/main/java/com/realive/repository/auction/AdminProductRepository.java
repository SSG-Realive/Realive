package com.realive.repository.auction;

import com.realive.domain.auction.AdminProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AdminProductRepository extends JpaRepository<AdminProduct, Integer> {

    Optional<AdminProduct> findById(Integer id);

    Optional<AdminProduct> findByProductId(Integer productId);

    @Query("select ap.id, ap.productId, ap.purchasedFromSellerId, ap.purchasedAt, " +
            "  ap.isAuctioned " +
            " from AdminProduct ap ")
    Page<Object[]> list1(Pageable pageable);
}


