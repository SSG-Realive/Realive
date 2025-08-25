package com.realive.serviceimpl.admin.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.common.enums.AuctionStatus;
import com.realive.domain.auction.Bid;
import com.realive.domain.customer.Customer;
import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.BidRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.service.admin.auction.BidService;
//import com.realive.service.notification.NotificationService;
import com.realive.util.TickSizeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final CustomerRepository customerRepository;
    private final TickSizeCalculator tickSizeCalculator;
//    private final NotificationService notificationService;

    // 동시성 제어
    @Override
    public BidResponseDTO placeBid(Integer auctionId, Long customerId, BidRequestDTO requestDTO) {
        log.info("🎯 입찰 시작 - 경매ID: {}, 고객ID: {}, 입찰가: {}", auctionId, customerId, requestDTO.getBidPrice());
        
        Auction auction = auctionRepository.findByIdWithLock(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
        
        Customer customer = customerRepository.findById(customerId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));

        validateAuction(auction);
        validateBidAmount(auction, requestDTO.getBidPrice());

        // 같은 금액으로 연속 입찰 불가
        bidRepository.findTopByAuctionIdAndCustomerIdOrderByBidTimeDesc(auctionId, customerId)
                .ifPresent(lastBid -> {
                    if (lastBid.getBidPrice().equals(requestDTO.getBidPrice())) {
                        throw new IllegalArgumentException("동일 금액으로 연속 입찰할 수 없습니다.");
                    }
                });

        // 경매에 동일 금액 입찰자 체크
        if (bidRepository.existsByAuctionIdAndBidPrice(auctionId, requestDTO.getBidPrice())) {
            throw new IllegalArgumentException("이미 해당 금액으로 입찰한 사용자가 있습니다.");
        }

        Bid bid = Bid.builder()
                .auctionId(auctionId)
                .customerId(customerId)
                .bidPrice(requestDTO.getBidPrice())
                .bidTime(LocalDateTime.now())
                .build();

        Bid savedBid = bidRepository.save(bid);

        // 경매 현재가 및 입찰 수 업데이트
        log.info("🔥 현재가 업데이트 전: {} -> 후: {}", auction.getCurrentPrice(), requestDTO.getBidPrice());
        auction.setCurrentPrice(requestDTO.getBidPrice());
        auction.setBidCount(auction.getBidCount() + 1);
        Auction updatedAuction = auctionRepository.save(auction);
        log.info("🔥 경매 저장 완료. 업데이트된 현재가: {}, 입찰 수: {}", updatedAuction.getCurrentPrice(), updatedAuction.getBidCount());

        // 이전 입찰자에게 알림
//        if (auction.getCurrentPrice() != null && auction.getCurrentPrice() < requestDTO.getBidPrice()) {
//            // 현재 입찰자 ID를 가져오기 위해 가장 최근 입찰 조회
//            Bid currentBid = bidRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId)
//                    .orElse(null);
//
//            if (currentBid != null && !currentBid.getCustomerId().equals(customerId)) {
//                notificationService.sendBidOutbidNotification(
//                    currentBid.getCustomerId(),
//                    auctionId,
//                    requestDTO.getBidPrice()
//                );
//            }
//        }

        BidResponseDTO response = BidResponseDTO.fromEntity(savedBid, customer.getName());
        log.info("✅ 입찰 완료 - 경매ID: {}, 고객ID: {}, 입찰가: {}", auctionId, customerId, requestDTO.getBidPrice());
        return response;
    }

    @Override
    public List<BidResponseDTO> getBidsForAuction(Integer auctionId) {
        return bidRepository.findByAuctionId(auctionId)
                .stream()
                .map(bid -> {
                    Customer customer = customerRepository.findById(bid.getCustomerId().longValue())
                            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));
                    return BidResponseDTO.fromEntity(bid, customer.getName());
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<BidResponseDTO> getBidsByAuction(Integer auctionId, Pageable pageable) {
        return bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId, pageable)
                .map(bid -> {
                    Customer customer = customerRepository.findById(bid.getCustomerId().longValue())
                            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));
                    return BidResponseDTO.fromEntity(bid, customer.getName());
                });
    }

    @Override
    public Page<BidResponseDTO> getBidsByCustomer(Long customerId, Pageable pageable) {
        return bidRepository.findByCustomerIdOrderByBidTimeDesc(customerId, pageable)
                .map(bid -> {
                    Customer customer = customerRepository.findById(customerId.longValue())
                            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));
                    return BidResponseDTO.fromEntity(bid, customer.getName());
                });
    }

    @Override
    public Page<BidResponseDTO> getAllBids(Pageable pageable) {
        return bidRepository.findAllByOrderByBidTimeDesc(pageable)
                .map(bid -> {
                    Customer customer = customerRepository.findById(bid.getCustomerId().longValue())
                            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));
                    return BidResponseDTO.fromEntity(bid, customer.getName());
                });
    }

    private void validateAuction(Auction auction) {
        if (auction.getStatus() != AuctionStatus.PROCEEDING) {
            throw new IllegalStateException("경매가 진행 중이 아닙니다.");
        }
    }

    private void validateBidAmount(Auction auction, Integer bidAmount) {
        int currentPrice = auction.getCurrentPrice();
        int startPrice = auction.getStartPrice();

        int tickSize = tickSizeCalculator.calculateTickSize(startPrice);
        int minBidPrice = currentPrice + tickSize;

        if (bidAmount < minBidPrice) {
            throw new IllegalArgumentException("입찰 금액은 최소 입찰 단위 이상이어야 합니다. (최소: " + minBidPrice + "원)");
        }
        // 틱 단위를 맞추지 않은 입찰 거절
        if ((bidAmount - currentPrice) % tickSize != 0) {
            throw new IllegalArgumentException("입찰 금액은 " + tickSize + "원 단위로 가능합니다.");
        }
    }
}
