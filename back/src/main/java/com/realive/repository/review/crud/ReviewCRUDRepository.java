package com.realive.repository.review.crud;

import com.realive.domain.seller.SellerReview;
import org.springframework.data.jpa.repository.JpaRepository; // CrudRepository 대신 JpaRepository 사용 권장
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// CrudRepository 대신 JpaRepository 사용을 권장합니다.
// JpaRepository는 CrudRepository의 기능을 포함하며, 페이징/정렬 등 더 많은 기능을 제공합니다.
// 이후 페이징 조회 등이 필요할 수 있으므로 미리 변경하는 것이 좋습니다.
public interface ReviewCRUDRepository extends JpaRepository<SellerReview, Long> {

    // ⭐ 수정: customerId, orderId 뿐만 아니라 sellerId까지 함께 조회하여 중복 리뷰 여부 확인
    Optional<SellerReview> findByOrderIdAndCustomerIdAndSellerId(Long orderId, Long customerId, Long sellerId);

    // Optional<SellerReview> findByCustomerIdAndOrderId(Long customerId, Long orderId); // 기존 메서드는 필요하면 유지
}