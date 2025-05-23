package com.realive.repository.admin.approval;

import com.realive.domain.seller.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Seller, Integer>, JpaSpecificationExecutor<Seller> {

    Optional<Seller> findById(Integer sellerId);

    Page<Seller> findByStatus(SellerApprovalStatusByAdmin status, Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    Page<Seller> findByNameContainingIgnoreCase(String nameKeyword, Pageable pageable);

}
