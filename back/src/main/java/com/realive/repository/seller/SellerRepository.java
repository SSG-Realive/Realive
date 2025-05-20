package com.realive.repository.seller;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realive.domain.seller.Seller;
import com.realive.repository.common.EmailLookupRepository;

public interface SellerRepository extends JpaRepository<Seller,Long>, EmailLookupRepository<Seller> {

    boolean existsByEmail(String email);
    boolean existsByName(String name);
    
}
