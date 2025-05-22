package com.realive.repository.review.crud;

import com.realive.domain.review.SellerReview;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewCRUDRepository extends CrudRepository<SellerReview, Long> {
    Optional<SellerReview> findByCustomerIdAndOrderId(Long customerId, Long orderId);
}