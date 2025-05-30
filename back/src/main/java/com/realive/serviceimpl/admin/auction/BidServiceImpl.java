//package com.realive.serviceimpl.admin.auction;
//
//
//
//import com.realive.domain.auction.Auction;
//import com.realive.domain.auction.Bid;
//import com.realive.domain.customer.Customer;
//import com.realive.dto.bid.BidRequestDTO;
//import com.realive.dto.bid.BidResponseDTO;
//import com.realive.repository.auction.AuctionRepository;
//import com.realive.repository.auction.BidRepository;
//import com.realive.repository.customer.CustomerRepository;
//import com.realive.service.admin.auction.BidService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.NoSuchElementException;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class BidServiceImpl implements BidService {
//
//    private final BidRepository bidRepository;
//    private final AuctionRepository auctionRepository;
//    private final CustomerRepository customerRepository;
//
//    @Override
//    @Transactional
//    public BidResponseDTO placeBid(BidRequestDTO requestDto, Long customerIdFromAuth) {
//        log.info("입찰 시도 처리 시작 - AuctionId: {}, BidPrice: {}, AuthenticatedCustomerId: {}",
//                requestDto.getAuctionId(), requestDto.getBidPrice(), customerIdFromAuth);
//
//        Customer customer = customerRepository.findById(customerIdFromAuth)
//                .orElseThrow(() -> new NoSuchElementException("고객 정보를 찾을 수 없습니다. ID: " + customerIdFromAuth));
//
//        if (!customer.getIsActive()) {
//            log.warn("입찰 실패 - 고객(ID:{})이 활성 상태가 아님.", customerIdFromAuth);
//            throw new AccessDeniedException("현재 입찰할 수 없는 계정 상태입니다 (계정 비활성).");
//        }
//
//        Auction auction = auctionRepository.findById(requestDto.getAuctionId())
//                .orElseThrow(() -> new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + requestDto.getAuctionId()));
//
//        if (auction.isClosed()) {
//            throw new IllegalStateException("이미 종료된 경매입니다.");
//        }
//        LocalDateTime now = LocalDateTime.now();
//        if (now.isAfter(auction.getEndTime())) {
//            throw new IllegalStateException("경매 시간이 종료되었습니다.");
//        }
//        if (now.isBefore(auction.getStartTime())) {
//            throw new IllegalStateException("아직 시작되지 않은 경매입니다.");
//        }
//
//        if (requestDto.getBidPrice() <= auction.getCurrentPrice()) {
//            throw new IllegalArgumentException("입찰 가격은 현재 최고가보다 높아야 합니다.");
//        }
//
//        Integer tickSize = auction.getTickSize();
//        if (tickSize == null || tickSize <= 0) {
//            tickSize = 100; // 기본 최소 입찰 단위
//        }
//        if (auction.getCurrentPrice() > auction.getStartPrice() &&
//                (requestDto.getBidPrice() - auction.getCurrentPrice()) % tickSize != 0) {
//            throw new IllegalArgumentException("입찰 가격은 정해진 입찰 단위(현재 최고가에서 " + tickSize + "원 단위)에 맞아야 합니다.");
//        }
//
//        Bid newBid = Bid.builder()
//                .auctionId(auction.getId())
//                .customerId(customerIdFromAuth.intValue())
//                .bidPrice(requestDto.getBidPrice())
//                .bidTime(LocalDateTime.now())
//                .build();
//        Bid savedBid = bidRepository.save(newBid);
//
//        auction.setCurrentPrice(savedBid.getBidPrice());
//        auctionRepository.save(auction);
//
//        log.info("입찰 성공 및 경매 현재가 업데이트 완료 - BidId: {}, AuctionId: {}, NewCurrentPrice: {}",
//                savedBid.getId(), auction.getId(), auction.getCurrentPrice());
//        return BidResponseDTO.fromEntity(savedBid);
//    }
//
//    @Override
//    public Page<BidResponseDTO> getBidsForAuction(Integer auctionId, Pageable pageable) {
//        log.info("특정 경매의 입찰 내역 조회 처리 - AuctionId: {}, Pageable: {}", auctionId, pageable);
//        if (!auctionRepository.existsById(auctionId)) {
//            throw new NoSuchElementException("해당 경매 정보를 찾을 수 없습니다. ID: " + auctionId);
//        }
//        Page<Bid> bidPage = bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId, pageable);
//
//        List<BidResponseDTO> bidResponseDTOs = bidPage.getContent().stream()
//                .map(BidResponseDTO::fromEntity)
//                .collect(Collectors.toList());
//        return new PageImpl<>(bidResponseDTOs, pageable, bidPage.getTotalElements());
//    }
//
//    @Override
//    public Page<BidResponseDTO> getBidsByCustomer(Long customerId, Pageable pageable) {
//        log.info("특정 고객의 입찰 내역 조회 처리 - CustomerId: {}, Pageable: {}", customerId, pageable);
//        if (!customerRepository.existsById(customerId)) {
//            throw new NoSuchElementException("해당 고객 정보를 찾을 수 없습니다. ID: " + customerId);
//        }
//        Page<Bid> bidPage = bidRepository.findByCustomerIdOrderByBidTimeDesc(customerId.intValue(), pageable);
//
//        List<BidResponseDTO> bidResponseDTOs = bidPage.getContent().stream()
//                .map(BidResponseDTO::fromEntity)
//                .collect(Collectors.toList());
//        return new PageImpl<>(bidResponseDTOs, pageable, bidPage.getTotalElements());
//    }
//}
