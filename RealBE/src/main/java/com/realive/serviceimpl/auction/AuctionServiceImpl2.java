package com.realive.serviceimpl.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.auction.Bid;
import com.realive.domain.product.Product;
import com.realive.domain.product.ProductImage;
import com.realive.domain.common.enums.MediaType;
import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.auction.AuctionWinnerResponseDTO;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.BidRepository;
import com.realive.repository.auction.AuctionPaymentRepository;
import com.realive.repository.product.ProductImageRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.service.auction.AuctionService2;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionServiceImpl2 implements AuctionService2 {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final AuctionPaymentRepository auctionPaymentRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public AuctionWinnerResponseDTO getWinnerAuctionDetail(Long customerId, Integer auctionId) {
        // 1) 검증 및 낙찰 경매 조회
        Auction auction = auctionRepository
            .findWonAuctionByUserIdAndAuctionId(customerId, auctionId)
            .orElseThrow(() -> new EntityNotFoundException("해당 낙찰 경매를 찾을 수 없습니다."));

        // 2) 최종 낙찰 입찰 내역 조회
        Bid winningBid = bidRepository
            .findTopByAuctionIdAndCustomerIdOrderByBidTimeDesc(auctionId, customerId)
            .orElseThrow(() -> new IllegalStateException("낙찰 입찰 내역이 없습니다."));

        // 3) AdminProduct → Product 및 썸네일 URL 조회
        var adminProduct = auction.getAdminProduct();
        Long productId = adminProduct.getProductId().longValue();

        Product product = productRepository
            .findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("연결된 상품을 찾을 수 없습니다."));

        String imageThumbnailUrl = productImageRepository
            .findFirstByProductIdAndIsThumbnailTrueAndMediaType(productId, MediaType.IMAGE)
            .map(ProductImage::getUrl)
            .orElse(null);

        List<String> imageUrls = productImageRepository.findUrlsByProductId(product.getId());

        AdminProductDTO productDTO = AdminProductDTO.fromEntity(
            adminProduct,
            product,
            imageThumbnailUrl,
                imageUrls
        );

        // 4) 결제 상태 확인
        boolean isPaid = auctionPaymentRepository.existsByCustomerIdAndAuctionIdAndStatusCompleted(customerId, auctionId);
        String paymentStatus = isPaid ? "결제완료" : "결제대기";

        // 5) 최종 응답 DTO 생성 및 반환
        AuctionWinnerResponseDTO response = AuctionWinnerResponseDTO.fromEntity(auction, productDTO, winningBid);
        response.setPaid(isPaid);
        response.setPaymentStatus(paymentStatus);
        
        return response;
    }
}
