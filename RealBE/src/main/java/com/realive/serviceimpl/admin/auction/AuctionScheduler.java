package com.realive.serviceimpl.admin.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.auction.Bid;
import com.realive.domain.common.enums.AuctionStatus;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.BidRepository;
import com.realive.repository.auction.AuctionPaymentRepository;
// import com.realive.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionScheduler {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final AuctionPaymentRepository auctionPaymentRepository;
    // private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void completeEndedAuctions() {
        log.debug("🛎️ [Auction Scheduler] 실행_{}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();

        // 종료 시간이 지난 진행 중인 경매 조회
        List<Auction> endedAuctions = auctionRepository.findByStatusAndEndTimeBefore(
                AuctionStatus.PROCEEDING, now);

        if (endedAuctions.isEmpty()) {
            log.debug("🔕 처리할 경매 없음");
            return;
        }

        for (Auction auction : endedAuctions) {
            try {
                // 최고 입찰 조회 (동점 가능성 있음)
                List<Bid> winningBids = bidRepository.findTopByAuctionIdOrderByBidPriceDesc(auction.getId());
                Bid winningBid = !winningBids.isEmpty() ? winningBids.get(0) : null;

                if (winningBid != null) {
                    // 낙찰 처리
                    auction.setStatus(AuctionStatus.COMPLETED);
                    auction.setWinningBidPrice(winningBid.getBidPrice());
                    auction.setWinningCustomerId(winningBid.getCustomerId());
                    auctionRepository.save(auction);

                    // 알림 (옵션)
                    // notificationService.sendAuctionWinNotification(...);

                    log.info("✅ 경매 종료 처리 완료 - 경매ID: {}, 낙찰자ID: {}, 낙찰가: {}",
                            auction.getId(), winningBid.getCustomerId(), winningBid.getBidPrice());
                } else {
                    // 입찰자 없는 경우 유찰 처리
                    auction.setStatus(AuctionStatus.FAILED);
                    auctionRepository.save(auction);

                    log.info("⚠️ 경매 유찰 처리 완료 - 경매ID: {}, 사유: 입찰자 없음", auction.getId());
                }
            } catch (Exception e) {
                log.error("❌ 경매 종료 처리 중 오류 발생 - 경매ID: {}", auction.getId(), e);
            }
        }

        log.debug("✅ [Scheduler] 경매 스케줄러 실행 완료 at {}", LocalDateTime.now());
    }

    @Scheduled(fixedRate = 300000) // 5분마다 실행
    @Transactional
    public void handleExpiredPayments() {
        log.debug("💳 [Payment Deadline Scheduler] 실행_{}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();

        // 결제 마감이 지난 완료된 경매 조회 (결제 마감: 경매 종료 후 7일)
        List<Auction> expiredPaymentAuctions = auctionRepository.findByStatusAndEndTimeBefore(
                AuctionStatus.COMPLETED, now.minusDays(7));

        if (expiredPaymentAuctions.isEmpty()) {
            log.debug("🔕 결제 마감 처리할 경매 없음");
            return;
        }

        for (Auction auction : expiredPaymentAuctions) {
            try {
                // 결제 완료 여부 확인
                boolean isPaid = auctionPaymentRepository.existsByCustomerIdAndAuctionIdAndStatusCompleted(
                        auction.getWinningCustomerId(), auction.getId());

                if (!isPaid) {
                    // 결제 미완료 시 유찰 처리
                    auction.setStatus(AuctionStatus.FAILED);
                    auction.setWinningCustomerId(null);
                    auction.setWinningBidPrice(null);
                    auctionRepository.save(auction);

                    log.info("⚠️ 결제 마감으로 인한 유찰 처리 완료 - 경매ID: {}, 낙찰자ID: {}", 
                            auction.getId(), auction.getWinningCustomerId());
                }
            } catch (Exception e) {
                log.error("❌ 결제 마감 처리 중 오류 발생 - 경매ID: {}", auction.getId(), e);
            }
        }

        log.debug("✅ [Payment Deadline Scheduler] 결제 마감 스케줄러 실행 완료 at {}", LocalDateTime.now());
    }
}