package com.realive.serviceimpl.admin.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.common.enums.AuctionStatus;
import com.realive.domain.auction.Bid;
import com.realive.domain.customer.Customer;
import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.exception.BidException;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.BidRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.service.admin.auction.BidService;
import com.realive.util.TickSizeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final CustomerRepository customerRepository;
    private final TickSizeCalculator tickSizeCalculator;

    // 동시성 제어
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BidResponseDTO placeBid(Integer auctionId, Integer customerId, BidRequestDTO requestDTO) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                log.info("입찰 처리 시작 - 경매ID: {}, 고객ID: {}, 입찰가: {}", 
                    auctionId, customerId, requestDTO.getBidPrice());
                
                // 경매 조회 (Pessimistic Lock 적용)
                Auction auction = auctionRepository.findByIdWithLock(auctionId)
                    .orElseThrow(() -> new BidException("존재하지 않는 경매입니다."));
                
                // 경매 상태 확인
                if (auction.getStatus() != AuctionStatus.PROCEEDING) {
                    throw new BidException("종료된 경매입니다.");
                }
                
                // 입찰가 검증
                int minBidPrice = tickSizeCalculator.calculateMinBidPrice(auction.getCurrentPrice(), auction.getStartPrice());
                if (requestDTO.getBidPrice() < minBidPrice) {
                    throw new BidException("입찰 단위가 맞지 않습니다.");
                }
                
                if (requestDTO.getBidPrice() <= auction.getCurrentPrice()) {
                    throw new BidException("현재가보다 높은 금액을 입력해주세요.");
                }
                
                // 고객 정보 조회
                Customer customer = customerRepository.findById(customerId.longValue())
                    .orElseThrow(() -> new BidException("존재하지 않는 고객입니다."));
                
                // 입찰 생성
                Bid bid = Bid.builder()
                    .auctionId(auction.getId())
                    .customerId(customerId)
                    .bidPrice(requestDTO.getBidPrice())
                    .build();
                
                // 입찰 저장
                bid = bidRepository.save(bid);
                
                // 경매 현재가 업데이트
                auction.setCurrentPrice(requestDTO.getBidPrice());
                auctionRepository.save(auction);
                
                log.info("입찰 처리 완료 - 입찰ID: {}, 고객명: {}", bid.getId(), customer.getName());
                
                return BidResponseDTO.fromEntity(bid, customer.getName());
                
            } catch (PessimisticLockingFailureException e) {
                retryCount++;
                log.warn("입찰 처리 재시도 {} - 경매ID: {}, 고객ID: {}", retryCount, auctionId, customerId);
                
                if (retryCount == maxRetries) {
                    log.error("입찰 처리 최대 재시도 횟수 초과 - 경매ID: {}, 고객ID: {}", auctionId, customerId);
                    throw new BidException("입찰 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
                }
                
                try {
                    Thread.sleep(100); // 100ms 대기 후 재시도
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BidException("입찰 처리가 중단되었습니다.");
                }
            }
        }
        
        throw new BidException("입찰 처리 중 오류가 발생했습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidResponseDTO> getBidsForAuction(Integer auctionId) {
        return bidRepository.findByAuctionId(auctionId).stream()
            .map(bid -> {
                Customer customer = customerRepository.findById(bid.getCustomerId().longValue())
                    .orElseThrow(() -> new BidException("존재하지 않는 고객입니다."));
                return BidResponseDTO.fromEntity(bid, customer.getName());
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BidResponseDTO> getBidsByAuction(Integer auctionId, Pageable pageable) {
        Page<Bid> bids = bidRepository.findByAuctionId(auctionId, pageable);
        List<BidResponseDTO> bidDTOs = bids.getContent().stream()
            .map(bid -> {
                Customer customer = customerRepository.findById(bid.getCustomerId().longValue())
                    .orElseThrow(() -> new BidException("존재하지 않는 고객입니다."));
                return BidResponseDTO.fromEntity(bid, customer.getName());
            })
            .collect(Collectors.toList());
        return new PageImpl<>(bidDTOs, pageable, bids.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BidResponseDTO> getBidsByCustomer(Integer customerId, Pageable pageable) {
        Page<Bid> bids = bidRepository.findByCustomerId(customerId, pageable);
        List<BidResponseDTO> bidDTOs = bids.getContent().stream()
            .map(bid -> {
                Customer customer = customerRepository.findById(bid.getCustomerId().longValue())
                    .orElseThrow(() -> new BidException("존재하지 않는 고객입니다."));
                return BidResponseDTO.fromEntity(bid, customer.getName());
            })
            .collect(Collectors.toList());
        return new PageImpl<>(bidDTOs, pageable, bids.getTotalElements());
    }
}
