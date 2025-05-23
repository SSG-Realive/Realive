package com.realive.repository.seller;

import com.realive.domain.seller.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long>, JpaSpecificationExecutor<Seller> {

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    Optional<Seller> findByEmailAndIsActive(String email, boolean isActive);

    Page<Seller> findByIsApproved(boolean isApproved, Pageable pageable);

    Page<Seller> findByIsActive(boolean isActive, Pageable pageable);

    Page<Seller> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
