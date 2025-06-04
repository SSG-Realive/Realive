package com.realive.repository.review.crud;

import com.realive.domain.review.SellerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewCRUDRepository extends JpaRepository<SellerReview, Long> {

    Optional<SellerReview> findByOrder_IdAndCustomer_IdAndSeller_Id(Long orderId, Long customerId, Long sellerId);
}