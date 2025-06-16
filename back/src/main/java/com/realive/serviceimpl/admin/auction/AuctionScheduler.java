package com.realive.serviceimpl.admin.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.auction.Bid;
import com.realive.domain.common.enums.AuctionStatus;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.BidRepository;
import com.realive.service.notification.NotificationService;
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
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void completeEndedAuctions() {
        log.info("경매 종료 처리 시작");
        LocalDateTime now = LocalDateTime.now();
        
        // 종료 시간이 지난 진행 중인 경매 조회
        List<Auction> endedAuctions = auctionRepository.findByStatusAndEndTimeBefore(
            AuctionStatus.PROCEEDING, now);
        
        for (Auction auction : endedAuctions) {
            try {
                // 최고가 입찰 조회
                Bid winningBid = bidRepository.findTopByAuctionIdOrderByBidPriceDesc(auction.getId())
                    .orElse(null);
                
                if (winningBid != null) {
                    // 낙찰 처리
                    auction.setStatus(AuctionStatus.COMPLETED);
                    auctionRepository.save(auction);
                    
                    // 낙찰자에게 알림
                    notificationService.sendAuctionWinNotification(
                        winningBid.getCustomerId(),
                        auction.getId(),
                        winningBid.getBidPrice()
                    );
                    
                    log.info("경매 종료 처리 완료 - 경매ID: {}, 낙찰자ID: {}, 낙찰가: {}", 
                        auction.getId(), winningBid.getCustomerId(), winningBid.getBidPrice());
                } else {
                    // 입찰자가 없는 경우
                    auction.setStatus(AuctionStatus.CANCELLED);
                    auctionRepository.save(auction);
                    log.info("경매 취소 처리 완료 - 경매ID: {}, 사유: 입찰자 없음", auction.getId());
                }
            } catch (Exception e) {
                log.error("경매 종료 처리 중 오류 발생 - 경매ID: {}", auction.getId(), e);
            }
        }
    }
} 