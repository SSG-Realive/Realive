package com.realive.serviceimpl.admin.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.auction.Bid;
// import com.realive.domain.customer.Customer; // CustomerRepository 병합 후 주석 해제
import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.BidRepository;
// import com.realive.repository.customer.CustomerRepository; // CustomerRepository 병합 후 주석 해제
import com.realive.service.admin.auction.BidService; // BidService 인터페이스 경로
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    // private final CustomerRepository customerRepository; // CustomerRepository 병합 후 주석 해제 및 생성자에 추가

    @Override
    @Transactional
    public BidResponseDTO placeBid(BidRequestDTO requestDto, Long customerIdFromAuth) { // 인증된 사용자의 ID (Long)
        log.info("입찰 시도 - AuctionId: {}, BidPrice: {}, AuthenticatedCustomerId: {}",
                requestDto.getAuctionId(), requestDto.getBidPrice(), customerIdFromAuth);

        /*
        // CustomerRepository 병합 후 주석 해제 및 로직 활성화
        // 0. 고객 정보 조회 및 유효성 검증
        Customer customer = customerRepository.findById(customerIdFromAuth)
                .orElseThrow(() -> {
                    log.warn("입찰 실패 - 존재하지 않는 고객 ID: {}", customerIdFromAuth);
                    return new NoSuchElementException("고객 정보를 찾을 수 없습니다. ID: " + customerIdFromAuth);
                });

        // (추가) 고객 상태 검증 (예: 활성 상태인지, 입찰 가능한지 등)
        if (!customer.isActive() || customer.isBiddingRestricted()) { // Customer 엔티티에 해당 필드가 있다고 가정
            log.warn("입찰 실패 - 고객(ID:{})이 입찰할 수 없는 상태입니다.", customerIdFromAuth);
            throw new IllegalStateException("현재 입찰할 수 없는 상태입니다.");
        }
        */

        // 1. 경매 정보 조회
        Auction auction = auctionRepository.findById(requestDto.getAuctionId())
                .orElseThrow(() -> {
                    log.warn("입찰 실패 - 존재하지 않는 경매 ID: {}", requestDto.getAuctionId());
                    return new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + requestDto.getAuctionId());
                });

        // 2. 경매 상태 검증
        if (auction.isClosed()) {
            log.warn("입찰 실패 - 이미 종료된 경매입니다. AuctionId: {}", auction.getId());
            throw new IllegalStateException("이미 종료된 경매입니다.");
        }
        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            log.warn("입찰 실패 - 경매 시간이 종료되었습니다. AuctionId: {}", auction.getId());
            // TODO: 경매 종료 처리 로직 (별도 스케줄러 또는 여기서 즉시 처리)
            // auction.setClosed(true);
            // auctionRepository.save(auction);
            throw new IllegalStateException("경매 시간이 종료되었습니다.");
        }
        if (LocalDateTime.now().isBefore(auction.getStartTime())) {
            log.warn("입찰 실패 - 아직 시작되지 않은 경매입니다. AuctionId: {}", auction.getId());
            throw new IllegalStateException("아직 시작되지 않은 경매입니다.");
        }

        // 3. 입찰 가격 검증
        if (requestDto.getBidPrice() <= auction.getCurrentPrice()) {
            log.warn("입찰 실패 - 현재 최고가({})보다 낮은 가격({})으로 입찰 시도. AuctionId: {}",
                    auction.getCurrentPrice(), requestDto.getBidPrice(), auction.getId());
            throw new IllegalArgumentException("입찰 가격은 현재 최고가보다 높아야 합니다.");
        }
        // TODO: 최소 입찰 증가 단위(tick size) 검증 로직 추가 필요

        // 4. Bid 엔티티 생성
        Bid newBid = Bid.builder()
                .auctionId(auction.getId())
                .customerId(customerIdFromAuth.intValue()) // Bid.customerId는 Integer, 인증된 ID는 Long으로 가정하여 변환
                .bidPrice(requestDto.getBidPrice())
                .bidTime(LocalDateTime.now())
                .build();

        // 5. Bid 저장
        Bid savedBid = bidRepository.save(newBid);
        log.info("입찰 성공 - BidId: {}, AuctionId: {}, CustomerId: {}, BidPrice: {}",
                savedBid.getId(), savedBid.getAuctionId(), savedBid.getCustomerId(), savedBid.getBidPrice());

        // 6. Auction의 현재 가격 업데이트
        auction.setCurrentPrice(savedBid.getBidPrice());
        auctionRepository.save(auction);
        log.info("경매(AuctionId: {}) 현재 가격 업데이트 완료: {}", auction.getId(), auction.getCurrentPrice());

        // TODO: (선택 사항) 실시간 알림 로직 (WebSocket 등)

        return BidResponseDTO.fromEntity(savedBid);
    }

    @Override
    public Page<BidResponseDTO> getBidsForAuction(Integer auctionId, Pageable pageable) {
        log.info("특정 경매의 입찰 내역 조회 - AuctionId: {}, Pageable: {}", auctionId, pageable);
        if (!auctionRepository.existsById(auctionId)) {
            log.warn("경매(ID:{}) 입찰 내역 조회 실패 - 존재하지 않는 경매", auctionId);
            throw new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + auctionId);
        }
        // BidRepository에 findByAuctionIdOrderByBidTimeDesc 또는 findByAuctionId(Integer, Pageable)가 있어야 함
        Page<Bid> bidPage = bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId, pageable);

        List<BidResponseDTO> bidResponseDTOs = bidPage.getContent().stream()
                .map(BidResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(bidResponseDTOs, pageable, bidPage.getTotalElements());
    }

    @Override
    public Page<BidResponseDTO> getBidsByCustomer(Long customerId, Pageable pageable) {
        log.info("특정 고객의 입찰 내역 조회 - CustomerId: {}, Pageable: {}", customerId, pageable);
        // BidRepository에 findByCustomerIdOrderByBidTimeDesc 또는 findByCustomerId(Integer, Pageable)가 있어야 함
        // Bid 엔티티의 customerId는 Integer이므로 customerId.intValue()로 변환
        Page<Bid> bidPage = bidRepository.findByCustomerIdOrderByBidTimeDesc(customerId.intValue(), pageable);

        List<BidResponseDTO> bidResponseDTOs = bidPage.getContent().stream()
                .map(BidResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(bidResponseDTOs, pageable, bidPage.getTotalElements());
    }
}
