package com.realive.serviceimpl.admin.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.auction.AdminProduct;
import com.realive.domain.product.Product;
// import com.realive.domain.common.enums.ProductStatus; // ProductStatus를 직접적인 조건으로 사용하지 않음
import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.AdminProductRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.service.admin.auction.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public AuctionResponseDTO registerAuction(AuctionCreateRequestDTO requestDto, Long authenticatedUserId) {
        log.info("경매 등록 요청 - DTO ProductId: {}, RequestingUserId: {}", requestDto.getProductId(), authenticatedUserId);

        AdminProduct adminProduct = adminProductRepository.findByProductId(requestDto.getProductId())
                .orElseThrow(() -> new NoSuchElementException("관리자 상품 목록에서 해당 상품을 찾을 수 없습니다. Product ID: " + requestDto.getProductId()));

        if (adminProduct.isAuctioned()) {
            throw new IllegalStateException("이미 경매에 등록된 상품입니다.");
        }

        Product originalProduct = productRepository.findById(adminProduct.getProductId().longValue())
                .orElseThrow(() -> new NoSuchElementException("경매 대상 원본 상품 정보를 찾을 수 없습니다."));

        // --- 권한 검증 로직 (실제 정책에 맞게 구현) ---
        // 예시: AdminProduct의 purchasedFromSellerId와 authenticatedUserId가 일치해야 함
        // if (adminProduct.getPurchasedFromSellerId() == null || !adminProduct.getPurchasedFromSellerId().equals(authenticatedUserId.intValue())) {
        //     throw new AccessDeniedException("해당 상품에 대한 경매 등록 권한이 없습니다.");
        // }
        // --- 권한 검증 로직 끝 ---

        Optional<Auction> existingActiveAuction = auctionRepository.findByProductIdAndIsClosedFalse(adminProduct.getProductId());
        if (existingActiveAuction.isPresent()) {
            throw new IllegalStateException("이미 해당 상품으로 진행 중인 다른 경매가 있습니다.");
        }

        // 상품 상태 검증: 비활성화된 상품은 경매 등록 불가
        if (!originalProduct.isActive()) {
            log.warn("경매 등록 실패 - Product ID {}의 상태가 부적절함 (활성: {})",
                    originalProduct.getId(), originalProduct.isActive());
            throw new IllegalStateException("경매에 등록할 수 없는 상품 상태입니다 (비활성).");
        }
        // ProductStatus Enum의 '상', '중', '하'를 사용하여 추가적인 상태 검증이 필요하다면 여기에 로직 추가
        // 예: if (originalProduct.getStatus() == ProductStatus.하) { throw new ... }

        Auction auction = requestDto.toEntity();
        Auction savedAuction = auctionRepository.save(auction);

        adminProduct.setAuctioned(true);
        adminProductRepository.save(adminProduct);

        // Product의 상태를 '경매중'으로 변경하는 로직 제거 (요청사항 반영)

        AdminProductDTO adminProductDtoForResponse = AdminProductDTO.fromEntity(adminProduct, originalProduct);
        return AuctionResponseDTO.fromEntity(savedAuction, adminProductDtoForResponse);
    }

    @Override
    public Page<AuctionResponseDTO> getActiveAuctions(Pageable pageable, String categoryFilter, String statusFilter) {
        log.info("진행 중인 경매 목록 조회 요청 - Pageable: {}, Category: {}, Status: {}", pageable, categoryFilter, statusFilter);
        LocalDateTime now = LocalDateTime.now();

        // TODO: categoryFilter, statusFilter를 사용한 동적 쿼리 생성 (JPQL/QueryDSL)
        Page<Auction> auctionPage = auctionRepository.findByIsClosedFalseAndEndTimeAfter(now, pageable);

        List<Integer> productIdsFromAuctions = auctionPage.getContent().stream()
                .map(Auction::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> productMap = Collections.emptyMap();
        if (!productIdsFromAuctions.isEmpty()) {
            List<Long> productLongIds = productIdsFromAuctions.stream().map(Integer::longValue).collect(Collectors.toList());
            List<Product> products = productRepository.findAllById(productLongIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        }

        Map<Integer, AdminProduct> adminProductMap = Collections.emptyMap();
        if (!productIdsFromAuctions.isEmpty()) {
            List<AdminProduct> adminProducts = adminProductRepository.findByProductIdIn(productIdsFromAuctions);
            adminProductMap = adminProducts.stream().collect(Collectors.toMap(AdminProduct::getProductId, ap -> ap));
        }

        Map<Long, Product> finalProductMap = productMap;
        Map<Integer, AdminProduct> finalAdminProductMap = adminProductMap;

        List<AuctionResponseDTO> auctionResponseDTOs = auctionPage.getContent().stream()
                .map(auctionEntity -> {
                    Product product = finalProductMap.get(auctionEntity.getProductId().longValue());
                    AdminProduct adminProduct = finalAdminProductMap.get(auctionEntity.getProductId());
                    AdminProductDTO adminProductDto = (adminProduct != null) ? AdminProductDTO.fromEntity(adminProduct, product) : null;
                    return AuctionResponseDTO.fromEntity(auctionEntity, adminProductDto);
                })
                .collect(Collectors.toList());

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
}
