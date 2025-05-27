package com.realive.serviceimpl.admin.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.auction.Bid;
import com.realive.domain.customer.Customer; // Customer 엔티티 import
import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.BidRepository;
import com.realive.repository.customer.CustomerRepository; // CustomerRepository import
import com.realive.service.admin.auction.BidService;
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
    private final CustomerRepository customerRepository; // CustomerRepository 주입

    @Override
    @Transactional
    public BidResponseDTO placeBid(BidRequestDTO requestDto, Long customerIdFromAuth) {
        log.info("입찰 시도 처리 시작 - AuctionId: {}, BidPrice: {}, AuthenticatedCustomerId: {}",
                requestDto.getAuctionId(), requestDto.getBidPrice(), customerIdFromAuth);

        // 0. 고객 정보 조회 및 상태 검증
        Customer customer = customerRepository.findById(customerIdFromAuth)
                .orElseThrow(() -> {
                    log.warn("입찰 실패 - 존재하지 않는 고객 ID: {}", customerIdFromAuth);
                    return new NoSuchElementException("입찰 사용자 정보를 찾을 수 없습니다."); // 외부 노출 메시지 일반화
                });

        // Customer 엔티티에 isActive() 메소드가 있다고 가정 (실제로는 getIsActive() 일 수 있음)
        if (!customer.getIsActive()) { // Customer.getIsActive() 사용
            log.warn("입찰 실패 - 고객(ID:{})이 활성 상태가 아닙니다.", customerIdFromAuth);
            throw new AccessDeniedException("현재 입찰할 수 없는 계정 상태입니다 (계정 비활성).");
        }
        // Customer 엔티티에 isBiddingRestricted() 메소드가 있다면 추가 검증 (예시)
        // if (customer.isBiddingRestricted()) {
        //     log.warn("입찰 실패 - 고객(ID:{})이 입찰 제한 상태입니다.", customerIdFromAuth);
        //     throw new AccessDeniedException("현재 입찰이 제한된 계정입니다.");
        // }

        // 1. 경매 정보 조회
        Auction auction = auctionRepository.findById(requestDto.getAuctionId())
                .orElseThrow(() -> {
                    log.warn("입찰 실패 - 존재하지 않는 경매 ID: {}", requestDto.getAuctionId());
                    return new NoSuchElementException("요청하신 경매 정보를 찾을 수 없습니다. ID: " + requestDto.getAuctionId());
                });

        // (선택적) 자기 상품 입찰 방지 로직 (Auction 엔티티에 getSellerId()가 있고, 타입이 Long이라고 가정)
        // if (auction.getSellerId() != null && auction.getSellerId().equals(customerIdFromAuth)) {
        //     log.warn("입찰 실패 - 판매자(ID:{})가 자신의 경매(ID:{})에 입찰 시도.", customerIdFromAuth, auction.getId());
        //     throw new IllegalStateException("자신의 경매에는 입찰할 수 없습니다.");
        // }

        // 2. 경매 상태 검증
        if (auction.isClosed()) { // Auction 엔티티에 isClosed() 메소드 가정
            log.warn("입찰 실패 - 이미 종료된 경매입니다. AuctionId: {}", auction.getId());
            throw new IllegalStateException("이미 종료된 경매입니다.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getEndTime())) {
            log.warn("입찰 실패 - 경매 시간이 종료되었습니다. AuctionId: {}", auction.getId());
            // 경매 종료 처리는 별도의 스케줄러나 이벤트 기반으로 처리하는 것이 더 안정적임
            // 여기서 auction.setClosed(true)로 바꾸는 것은 동시성 문제를 야기할 수 있음
            throw new IllegalStateException("경매 시간이 종료되었습니다.");
        }
        if (now.isBefore(auction.getStartTime())) {
            log.warn("입찰 실패 - 아직 시작되지 않은 경매입니다. AuctionId: {}", auction.getId());
            throw new IllegalStateException("아직 시작되지 않은 경매입니다.");
        }

        // 3. 입찰 가격 검증
        if (requestDto.getBidPrice() <= auction.getCurrentPrice()) { // Auction.getCurrentPrice() 가정
            log.warn("입찰 실패 - 현재 최고가({})보다 낮거나 같은 가격({})으로 입찰 시도. AuctionId: {}",
                    auction.getCurrentPrice(), requestDto.getBidPrice(), auction.getId());
            throw new IllegalArgumentException("입찰 가격은 현재 최고가보다 높아야 합니다.");
        }

        // 최소 입찰 증가 단위(tick size) 검증
        // Auction 엔티티에 getTickSize() 메소드가 있고, Integer 타입을 반환하며, null일 수 있다고 가정
        Integer tickSize = auction.getTickSize();
        if (tickSize == null || tickSize <= 0) {
            tickSize = 100; // 기본 최소 입찰 단위 (예: 100원) - 애플리케이션 설정값으로 관리하는 것이 좋음
            log.debug("경매(ID:{})에 tickSize가 설정되지 않아 기본값 {} 적용", auction.getId(), tickSize);
        }
        // 최초 입찰(auction.getCurrentPrice() == 0 또는 초기값)이 아닐 경우에만 tick size 검증
        if (auction.getCurrentPrice() > auction.getStartPrice() && // 시작가 이후의 입찰에 대해서만 적용
                (requestDto.getBidPrice() - auction.getCurrentPrice()) % tickSize != 0) {
            log.warn("입찰 실패 - 입찰 증가 단위({}) 불일치. CurrentPrice: {}, BidPrice: {}, AuctionId: {}",
                    tickSize, auction.getCurrentPrice(), requestDto.getBidPrice(), auction.getId());
            throw new IllegalArgumentException("입찰 가격은 정해진 입찰 단위(현재 최고가에서 " + tickSize + "원 단위)에 맞아야 합니다.");
        }


        // 4. Bid 엔티티 생성
        Bid newBid = Bid.builder()
                .auctionId(auction.getId())
                .customerId(customerIdFromAuth.intValue()) // Bid.customerId는 Integer, customerIdFromAuth는 Long
                .bidPrice(requestDto.getBidPrice())
                .bidTime(LocalDateTime.now()) // 입찰 시간은 서버 시간 기준
                .build(); // Bid 엔티티에 builder()가 있다고 가정

        // 5. Bid 저장
        Bid savedBid = bidRepository.save(newBid);
        log.info("입찰 성공 - BidId: {}, AuctionId: {}, CustomerId: {}, BidPrice: {}",
                savedBid.getId(), savedBid.getAuctionId(), savedBid.getCustomerId(), savedBid.getBidPrice());

        // 6. Auction의 현재 가격 업데이트
        auction.setCurrentPrice(savedBid.getBidPrice());
        // auction.setHighestBidderId(customerIdFromAuth); // Auction 엔티티에 최고 입찰자 ID 필드를 추가한다면
        auctionRepository.save(auction); // 변경된 현재 가격 저장
        log.info("경매(AuctionId: {}) 현재 가격 업데이트 완료: {}", auction.getId(), auction.getCurrentPrice());

        // TODO: (선택 사항) 입찰 성공 시 실시간 알림 로직 (WebSocket 등)

        return BidResponseDTO.fromEntity(savedBid); // BidResponseDTO에 fromEntity 정적 메소드 가정
    }

    @Override
    public Page<BidResponseDTO> getBidsForAuction(Integer auctionId, Pageable pageable) {
        log.info("특정 경매의 입찰 내역 조회 처리 - AuctionId: {}, Pageable: {}", auctionId, pageable);
        // 경매 존재 여부 확인
        if (!auctionRepository.existsById(auctionId)) {
            log.warn("경매(ID:{}) 입찰 내역 조회 실패 - 존재하지 않는 경매", auctionId);
            throw new NoSuchElementException("해당 경매 정보를 찾을 수 없습니다. ID: " + auctionId);
        }
        // BidRepository의 findByAuctionIdOrderByBidTimeDesc 사용 (Pageable의 sort는 무시될 수 있음)
        // 만약 Pageable의 sort를 우선시하려면 BidRepository 메소드명에서 OrderBy 제거 필요
        Page<Bid> bidPage = bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId, pageable);

        List<BidResponseDTO> bidResponseDTOs = bidPage.getContent().stream()
                .map(BidResponseDTO::fromEntity) // DTO에 fromEntity 정적 메소드 가정
                .collect(Collectors.toList());

        return new PageImpl<>(bidResponseDTOs, pageable, bidPage.getTotalElements());
    }

    @Override
    public Page<BidResponseDTO> getBidsByCustomer(Long customerId, Pageable pageable) {
        log.info("특정 고객의 입찰 내역 조회 처리 - CustomerId: {}, Pageable: {}", customerId, pageable);
        // 고객 존재 여부 확인
        if (!customerRepository.existsById(customerId)) { // CustomerRepository에 existsById가 있다고 가정 (기본 제공)
            throw new NoSuchElementException("해당 고객 정보를 찾을 수 없습니다. ID: " + customerId);
        }
        // Bid 엔티티의 customerId는 Integer이므로 customerId.intValue()로 변환
        Page<Bid> bidPage = bidRepository.findByCustomerIdOrderByBidTimeDesc(customerId.intValue(), pageable);

        List<BidResponseDTO> bidResponseDTOs = bidPage.getContent().stream()
                .map(BidResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(bidResponseDTOs, pageable, bidPage.getTotalElements());
    }
}
