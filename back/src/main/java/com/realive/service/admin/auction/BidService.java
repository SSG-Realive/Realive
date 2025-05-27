package com.realive.service.admin.auction;


import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.NoSuchElementException;

public interface BidService {

    /**
     * 새로운 입찰을 등록합니다.
     *
     * @param requestDto 입찰 요청 정보를 담은 DTO
     * @param customerId 입찰하는 고객의 ID (인증된 사용자로부터 가져옴)
     * @return 등록된 입찰 정보를 담은 DTO
     * @throws NoSuchElementException 경매 또는 고객 정보를 찾을 수 없을 경우
     * @throws IllegalStateException 경매가 진행 중이 아니거나, 입찰 가격이 유효하지 않을 경우
     */
    BidResponseDTO placeBid(BidRequestDTO requestDto, Long customerId);

    /**
     * 특정 경매의 모든 입찰 내역을 조회합니다. (페이징 가능)
     *
     * @param auctionId 입찰 내역을 조회할 경매 ID
     * @param pageable 페이징 정보
     * @return 해당 경매의 입찰 내역 Page 객체
     */
    Page<BidResponseDTO> getBidsForAuction(Integer auctionId, Pageable pageable);

    /**
     * 특정 고객의 모든 입찰 내역을 조회합니다. (페이징 가능)
     *
     * @param customerId 입찰 내역을 조회할 고객 ID
     * @param pageable 페이징 정보
     * @return 해당 고객의 입찰 내역 Page 객체
     */
    Page<BidResponseDTO> getBidsByCustomer(Long customerId, Pageable pageable);

}
