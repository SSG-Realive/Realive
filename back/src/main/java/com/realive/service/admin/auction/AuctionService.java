package com.realive.service.admin.auction;

import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface AuctionService {

    /**
     * (관리자에 의해) 새로운 경매를 등록합니다.
     *
     * @param requestDto 경매 생성 요청 정보를 담은 DTO
     * @param adminUserId 경매를 등록하는 관리자의 ID (인증된 관리자로부터 가져옴)
     * @return 등록된 경매 정보를 담은 DTO
     * @throws NoSuchElementException 상품을 찾을 수 없을 경우
     * @throws IllegalStateException 이미 해당 상품으로 진행 중인 경매가 있거나, 상품 상태가 부적절할 경우
     * @throws org.springframework.security.access.AccessDeniedException 관리자가 해당 작업을 수행할 권한이 없을 경우
     */
    AuctionResponseDTO registerAuction(AuctionCreateRequestDTO requestDto, Long adminUserId); // 파라미터명 변경: sellerUserId -> adminUserId

    /**
     * (관리자가) 현재 진행 중인 경매 목록을 페이징하여 조회합니다.
     * (이하 주석 동일)
     */
    Page<AuctionResponseDTO> getActiveAuctions(Pageable pageable, String categoryFilter, String statusFilter);

    /**
     * (관리자가) 특정 경매의 상세 정보를 조회합니다.
     * (이하 주석 동일)
     */
    AuctionResponseDTO getAuctionDetails(Integer auctionId);

    /**
     * (관리자가) 특정 판매자가 등록한 경매 목록을 페이징하여 조회합니다.
     * @param sellerId 조회 대상 판매자 ID
     * @param pageable 페이징 정보
     * @return 해당 판매자의 경매 목록 Page 객체
     */
    Page<AuctionResponseDTO> getAuctionsBySeller(Long sellerId, Pageable pageable);

    /**
     * (관리자가) 특정 상품 ID에 대해 현재 진행 중인 경매를 조회합니다.
     * (상품 하나당 하나의 활성 경매만 있다고 가정)
     * @param productId 상품 ID
     * @return 진행 중인 경매 정보 Optional 객체
     */
    Optional<AuctionResponseDTO> getCurrentAuctionForProduct(Integer productId);

}
