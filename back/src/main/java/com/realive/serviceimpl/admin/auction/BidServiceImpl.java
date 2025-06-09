package com.realive.serviceimpl.admin.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.auction.Bid;
import com.realive.domain.customer.Customer;
import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.BidRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.service.admin.auction.BidService;
import com.realive.util.TickSizeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public BidResponseDTO placeBid(BidRequestDTO requestDto, Long customerId) {
        log.info("입찰 요청 처리 시작 - CustomerId: {}, AuctionId: {}, BidPrice: {}", 
                customerId, requestDto.getAuctionId(), requestDto.getBidPrice());

        // 1. 경매 정보 조회 및 상태 검증
        Auction auction = auctionRepository.findById(requestDto.getAuctionId())
                .orElseThrow(() -> new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + requestDto.getAuctionId()));

        if (auction.isClosed()) {
            throw new IllegalStateException("이미 종료된 경매입니다.");
        }

        if (LocalDateTime.now().isBefore(auction.getStartTime())) {
            throw new IllegalStateException("아직 시작되지 않은 경매입니다.");
        }

        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            throw new IllegalStateException("이미 종료된 경매입니다.");
        }

        // 2. 고객 정보 조회 및 상태 검증
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("고객 정보를 찾을 수 없습니다. ID: " + customerId));

        if (!customer.getIsActive()) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        // 3. 입찰 단위 및 최소 입찰가 검증
        int tickSize = TickSizeCalculator.calculateTickSize(auction.getStartPrice());
        int minBidPrice = TickSizeCalculator.calculateMinBidPrice(
            auction.getCurrentPrice(), 
            auction.getStartPrice()
        );

        if (requestDto.getBidPrice() % tickSize != 0) {
            throw new IllegalArgumentException(
                String.format("입찰가는 %d원 단위로만 가능합니다.", tickSize)
            );
        }

        if (requestDto.getBidPrice() < minBidPrice) {
            throw new IllegalArgumentException(
                String.format("최소 입찰가는 %d원입니다.", minBidPrice)
            );
        }

        // 4. 입찰 정보 생성 및 저장
        Bid bid = Bid.builder()
                .auctionId(auction.getId())
                .customerId(customerId.intValue())
                .bidPrice(requestDto.getBidPrice())
                .bidTime(LocalDateTime.now())
                .build();

        Bid savedBid = bidRepository.save(bid);

        // 5. 경매 현재가 업데이트
        auction.setCurrentPrice(requestDto.getBidPrice());
        auctionRepository.save(auction);

        log.info("입찰 성공 - BidId: {}, AuctionId: {}, CustomerId: {}, BidPrice: {}", 
                savedBid.getId(), savedBid.getAuctionId(), savedBid.getCustomerId(), savedBid.getBidPrice());

        return BidResponseDTO.fromEntity(savedBid);
    }

    @Override
    public Page<BidResponseDTO> getBidsForAuction(Integer auctionId, Pageable pageable) {
        log.info("특정 경매의 입찰 내역 조회 처리 - AuctionId: {}, Pageable: {}", auctionId, pageable);
        if (!auctionRepository.existsById(auctionId)) {
            throw new NoSuchElementException("해당 경매 정보를 찾을 수 없습니다. ID: " + auctionId);
        }
        Page<Bid> bidPage = bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId, pageable);

        List<BidResponseDTO> bidResponseDTOs = bidPage.getContent().stream()
                .map(BidResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new PageImpl<>(bidResponseDTOs, pageable, bidPage.getTotalElements());
    }

    @Override
    public Page<BidResponseDTO> getBidsByCustomer(Long customerId, Pageable pageable) {
        log.info("특정 고객의 입찰 내역 조회 처리 - CustomerId: {}, Pageable: {}", customerId, pageable);
        if (!customerRepository.existsById(customerId)) {
            throw new NoSuchElementException("해당 고객 정보를 찾을 수 없습니다. ID: " + customerId);
        }
        Page<Bid> bidPage = bidRepository.findByCustomerIdOrderByBidTimeDesc(customerId.intValue(), pageable);

        List<BidResponseDTO> bidResponseDTOs = bidPage.getContent().stream()
                .map(BidResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new PageImpl<>(bidResponseDTOs, pageable, bidPage.getTotalElements());
    }

    @Override
    public Page<BidResponseDTO> getBidsByAuction(Long auctionId, Pageable pageable) {
        log.info("특정 경매의 입찰 내역 조회 처리 - AuctionId: {}, Pageable: {}", auctionId, pageable);
        Page<Bid> bids = bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId.intValue(), pageable);
        return bids.map(BidResponseDTO::fromEntity);
    }
}
