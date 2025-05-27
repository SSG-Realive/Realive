package com.realive.serviceimpl.admin.auction;


import com.realive.domain.auction.Auction;
import com.realive.domain.auction.AdminProduct;
import com.realive.domain.product.Product;
// import com.realive.domain.common.enums.ProductStatus;
import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.AdminProductRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.service.admin.auction.AuctionService; // 실제 AuctionService 인터페이스 경로
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final AdminProductRepository adminProductRepository;

    // --- 기존 메소드 (registerAuction, getActiveAuctions, getAuctionDetails)는 이전과 동일하게 유지 ---
    // (이전 답변의 코드를 그대로 사용하시면 됩니다. 여기서는 생략합니다.)
    // ... (registerAuction 메소드 구현) ...
    // ... (getActiveAuctions 메소드 구현 - categoryFilter, statusFilter는 여전히 TODO) ...
    // ... (getAuctionDetails 메소드 구현) ...

    @Override
    @Transactional // 데이터 변경이 있으므로 @Transactional 추가
    public AuctionResponseDTO registerAuction(AuctionCreateRequestDTO requestDto, Long authenticatedSellerId) {
        log.info("경매 등록 요청 - DTO ProductId: {}, AuthenticatedSellerId: {}", requestDto.getProductId(), authenticatedSellerId);

        AdminProduct adminProduct = adminProductRepository.findByProductId(requestDto.getProductId())
                .orElseThrow(() -> new NoSuchElementException("관리자 상품 목록에서 해당 상품을 찾을 수 없습니다. Product ID: " + requestDto.getProductId()));

        if (adminProduct.isAuctioned()) {
            throw new IllegalStateException("이미 경매에 등록된 상품입니다.");
        }

        Product originalProduct = productRepository.findById(adminProduct.getProductId().longValue())
                .orElseThrow(() -> new NoSuchElementException("경매 대상 원본 상품 정보를 찾을 수 없습니다."));

        // 권한 검증
        if (adminProduct.getPurchasedFromSellerId() == null || !adminProduct.getPurchasedFromSellerId().equals(authenticatedSellerId.intValue())) {
            log.warn("경매 등록 실패 - Product ID {}에 대한 권한 없음 (요청자 ID: {}, AdminProduct 판매자 ID: {}).",
                    adminProduct.getProductId(), authenticatedSellerId, adminProduct.getPurchasedFromSellerId());
            throw new AccessDeniedException("해당 상품에 대한 경매 등록 권한이 없습니다.");
        }

        Optional<Auction> existingActiveAuction = auctionRepository.findByProductIdAndIsClosedFalse(adminProduct.getProductId());
        if (existingActiveAuction.isPresent()) {
            throw new IllegalStateException("이미 해당 상품으로 진행 중인 다른 경매가 있습니다.");
        }

        if (!originalProduct.isActive()) {
            throw new IllegalStateException("경매에 등록할 수 없는 상품 상태입니다 (비활성).");
        }

        Auction auction = requestDto.toEntity();
        Auction savedAuction = auctionRepository.save(auction);
        log.info("경매 등록 성공 - AuctionId: {}, ProductId: {}", savedAuction.getId(), savedAuction.getProductId());

        adminProduct.setAuctioned(true);
        adminProductRepository.save(adminProduct);
        log.info("AdminProduct (ID: {})의 isAuctioned 상태를 true로 업데이트 완료.", adminProduct.getId());

        AdminProductDTO adminProductDtoForResponse = AdminProductDTO.fromEntity(adminProduct, originalProduct);
        return AuctionResponseDTO.fromEntity(savedAuction, adminProductDtoForResponse);
    }

    @Override
    public Page<AuctionResponseDTO> getActiveAuctions(Pageable pageable, String categoryFilter, String statusFilter) {
        log.info("진행 중인 경매 목록 조회 요청 - Pageable: {}, Category: {}, Status: {}", pageable, categoryFilter, statusFilter);
        LocalDateTime now = LocalDateTime.now();

        // TODO: categoryFilter, statusFilter DB 레벨 처리
        Page<Auction> auctionPage = auctionRepository.findByIsClosedFalseAndEndTimeAfter(now, pageable);

        // ... (이하 DTO 변환 로직은 이전과 동일, 여기서는 생략) ...
        // N+1 문제 해결 및 DTO 변환 로직은 이전 제안을 따릅니다.
        // 이 메소드는 현재 카테고리/상태 필터링이 DB 레벨에서 적용되지 않은 상태입니다.
        List<AuctionResponseDTO> auctionResponseDTOs = convertToAuctionResponseDTOs(auctionPage.getContent());
        return new PageImpl<>(auctionResponseDTOs, pageable, auctionPage.getTotalElements());
    }


    @Override
    public AuctionResponseDTO getAuctionDetails(Integer auctionId) {
        log.info("경매 상세 정보 조회 요청 - AuctionId: {}", auctionId);
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("경매를 찾을 수 없습니다. ID: " + auctionId));

        AdminProduct adminProduct = adminProductRepository.findByProductId(auction.getProductId())
                .orElseThrow(() -> new NoSuchElementException("경매에 연결된 관리자 상품 정보를 찾을 수 없습니다. Product ID: " + auction.getProductId()));

        Product product = productRepository.findById(auction.getProductId().longValue()).orElse(null);

        AdminProductDTO adminProductDto = AdminProductDTO.fromEntity(adminProduct, product);
        return AuctionResponseDTO.fromEntity(auction, adminProductDto);
    }


    // --- 새로 추가된 메소드 구현 ---

    @Override
    public Page<AuctionResponseDTO> getAuctionsBySeller(Long sellerId, Pageable pageable) {
        log.info("특정 판매자(ID:{})가 등록한 경매 목록 조회 요청. Pageable: {}", sellerId, pageable);

        // 1. 해당 판매자 ID를 가진 AdminProduct 목록 조회 (AdminProduct.purchasedFromSellerId 기준)
        // AdminProductRepository에 findByPurchasedFromSellerId(Integer sellerId, Pageable pageable) 와 같은 메소드가 필요하거나,
        // 모든 AdminProduct를 가져와서 필터링해야 함 (비효율적).
        // 여기서는 AdminProductRepository에 해당 메소드가 있다고 가정하고, productId 목록을 가져옵니다.
        // 또는, ProductRepository에서 sellerId로 Product 목록을 가져오고, 그 Product ID들로 Auction을 조회할 수도 있습니다.
        // 가장 직접적인 방법은 Auction 엔티티에 sellerId를 직접 저장하는 것이지만, 현재 구조에서는 AdminProduct를 거칩니다.

        // 방법 A: AdminProduct를 통해 productId 목록을 가져온 후 Auction 조회
        // AdminProductRepository에 List<AdminProduct> findByPurchasedFromSellerId(Integer sellerId); 필요
        List<AdminProduct> sellerAdminProducts = adminProductRepository.findByPurchasedFromSellerId(sellerId.intValue()); // intValue() 주의
        if (sellerAdminProducts.isEmpty()) {
            return Page.empty(pageable);
        }
        List<Integer> productIdsBySeller = sellerAdminProducts.stream()
                .map(AdminProduct::getProductId)
                .distinct()
                .collect(Collectors.toList());

        if (productIdsBySeller.isEmpty()) {
            return Page.empty(pageable);
        }

        // AuctionRepository에 Page<Auction> findByProductIdIn(List<Integer> productIds, Pageable pageable); 필요
        Page<Auction> auctionPage = auctionRepository.findByProductIdIn(productIdsBySeller, pageable);

        // DTO 변환 (getActiveAuctions 메소드의 DTO 변환 로직 재활용 또는 별도 메소드로 추출)
        List<AuctionResponseDTO> auctionResponseDTOs = convertToAuctionResponseDTOs(auctionPage.getContent());
        return new PageImpl<>(auctionResponseDTOs, pageable, auctionPage.getTotalElements());
    }

    @Override
    public Optional<AuctionResponseDTO> getCurrentAuctionForProduct(Integer productId) {
        log.info("특정 상품(ID:{})에 대해 현재 진행 중인 경매 조회 요청", productId);

        // AuctionRepository의 기존 메소드 활용
        Optional<Auction> auctionOptional = auctionRepository.findByProductIdAndIsClosedFalse(productId);

        // Optional<Auction>을 Optional<AuctionResponseDTO>로 변환
        return auctionOptional.map(auction -> {
            // Auction 상세 조회와 유사하게 AdminProduct 및 Product 정보 로드
            AdminProduct adminProduct = adminProductRepository.findByProductId(auction.getProductId())
                    .orElse(null); // 없을 수도 있지만, Auction이 있다면 AdminProduct도 있어야 정상
            Product product = null;
            if (adminProduct != null) {
                product = productRepository.findById(adminProduct.getProductId().longValue()).orElse(null);
            }
            AdminProductDTO adminProductDto = (adminProduct != null) ? AdminProductDTO.fromEntity(adminProduct, product) : null;
            return AuctionResponseDTO.fromEntity(auction, adminProductDto);
        });
    }

    // --- DTO 변환을 위한 헬퍼 메소드 (중복 제거용) ---
    private List<AuctionResponseDTO> convertToAuctionResponseDTOs(List<Auction> auctions) {
        if (auctions == null || auctions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> productIds = auctions.stream()
                .map(Auction::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> productMap = Collections.emptyMap();
        if (!productIds.isEmpty()) {
            List<Long> productLongIds = productIds.stream().map(Integer::longValue).collect(Collectors.toList());
            List<Product> products = productRepository.findAllById(productLongIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        }

        Map<Integer, AdminProduct> adminProductMap = Collections.emptyMap();
        if (!productIds.isEmpty()) {
            List<AdminProduct> adminProducts = adminProductRepository.findByProductIdIn(productIds);
            adminProductMap = adminProducts.stream().collect(Collectors.toMap(AdminProduct::getProductId, ap -> ap));
        }

        Map<Long, Product> finalProductMap = productMap;
        Map<Integer, AdminProduct> finalAdminProductMap = adminProductMap;

        return auctions.stream()
                .map(auctionEntity -> {
                    Product product = finalProductMap.get(auctionEntity.getProductId().longValue());
                    AdminProduct adminProduct = finalAdminProductMap.get(auctionEntity.getProductId());
                    AdminProductDTO adminProductDto = (adminProduct != null) ? AdminProductDTO.fromEntity(adminProduct, product) : null;
                    return AuctionResponseDTO.fromEntity(auctionEntity, adminProductDto);
                })
                .collect(Collectors.toList());
    }
}
