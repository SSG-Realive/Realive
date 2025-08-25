package com.realive.repository.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.common.enums.AuctionStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public class AuctionRepositoryImpl implements AuctionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Auction> findWonAuctionByUserIdAndAuctionId(Long userId, Integer auctionId) {
        String jpql = """
            select a
              from Auction a
             join fetch a.adminProduct p
             where a.id = :auctionId
               and a.winningCustomerId = :userId
               and a.status = :completedStatus
        """;

        TypedQuery<Auction> query = em.createQuery(jpql, Auction.class)
            .setParameter("auctionId", auctionId)                          // 경매 ID
            .setParameter("userId", userId)                                // 고객 ID
            .setParameter("completedStatus", AuctionStatus.COMPLETED);     // 상태 = COMPLETED

        // List로 받아서 첫 건만 Optional로
        Auction auction = query
            .getResultList()
            .stream()
            .findFirst()
            .orElse(null);

        return Optional.ofNullable(auction);
    }
}
