package com.realive.service.admin.auction;

import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BidService {
    /**
     * 경매에 입찰합니다.
     *
     * @param auctionId 경매 ID
     * @param customerId 입찰자 ID
     * @param requestDTO 입찰 요청 정보
     * @return 입찰 결과
     */
    BidResponseDTO placeBid(Integer auctionId, Long customerId, BidRequestDTO requestDTO);

    /**
     * 특정 경매의 입찰 내역을 조회합니다.
     *
     * @param auctionId 경매 ID
     * @return 입찰 내역 목록
     */
    List<BidResponseDTO> getBidsForAuction(Integer auctionId);

    /**
     * 특정 경매의 입찰 내역을 페이징하여 조회합니다.
     *
     * @param auctionId 경매 ID
     * @param pageable 페이징 정보
     * @return 입찰 내역 페이지
     */
    Page<BidResponseDTO> getBidsByAuction(Integer auctionId, Pageable pageable);

    /**
     * 특정 고객의 입찰 내역을 페이징하여 조회합니다.
     *
     * @param customerId 고객 ID
     * @param pageable 페이징 정보
     * @return 입찰 내역 페이지
     */
    Page<BidResponseDTO> getBidsByCustomer(Long customerId, Pageable pageable);

    /**
     * 전체 입찰 내역을 페이징하여 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 입찰 내역 페이지
     */
    Page<BidResponseDTO> getAllBids(Pageable pageable);
}
