package com.realive.repository.review.view;

import com.realive.domain.seller.SellerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewViewRepository extends JpaRepository<SellerReview, Long>, ReviewDetail {

}