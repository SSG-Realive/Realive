package com.realive.repository.review.crud;

import com.realive.domain.review.SellerReview;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCRUDRepository extends CrudRepository<SellerReview, Long> {

}
