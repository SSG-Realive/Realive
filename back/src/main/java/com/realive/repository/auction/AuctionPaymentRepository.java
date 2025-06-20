package com.realive.repository.auction;

import com.realive.domain.payment.AuctionPayment;
import com.realive.domain.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionPaymentRepository extends JpaRepository<AuctionPayment, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM AuctionPayment ap WHERE ap.customerId = :customerId AND ap.auctionId = :auctionId AND ap.status = 'COMPLETED')")
    boolean existsByCustomerIdAndAuctionIdAndStatusCompleted(@Param("customerId") Long customerId, @Param("auctionId") Integer auctionId);

    Optional<AuctionPayment> findByPaymentKey(String paymentKey);

    Optional<AuctionPayment> findByAuctionIdAndCustomerId(Integer auctionId, Long customerId);
} 