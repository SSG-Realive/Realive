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
//    private final NotificationService notificationService;

    // 동시성 제어
    @Override
    public BidResponseDTO placeBid(Integer auctionId, Integer customerId, BidRequestDTO requestDTO) {
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


        Bid bid = Bid.builder()
                .auctionId(auctionId)
                .customerId(customerId)
                .bidPrice(requestDTO.getBidPrice())
                .bidTime(LocalDateTime.now())
                .build();

        Bid savedBid = bidRepository.save(bid);

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

        return BidResponseDTO.fromEntity(savedBid, customer.getName());
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
    public Page<BidResponseDTO> getBidsByCustomer(Integer customerId, Pageable pageable) {
        return bidRepository.findByCustomerIdOrderByBidTimeDesc(customerId, pageable)
                .map(bid -> {
                    Customer customer = customerRepository.findById(customerId.longValue())
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
        if (bidAmount <= auction.getCurrentPrice()) {
            throw new IllegalArgumentException("입찰 금액은 현재 가격보다 높아야 합니다.");
        }
    }
}
