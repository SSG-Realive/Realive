package com.realive.serviceimpl.admin.auction;


import com.realive.domain.auction.Auction;
import com.realive.domain.auction.AdminProduct;
import com.realive.domain.product.Product;
import com.realive.domain.common.enums.ProductStatus; // ProductStatus Enum import
// import com.realive.domain.seller.Seller; // Product 엔티티가 Seller를 직접 참조하고, 소유권 검증에 사용 시 필요
import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.AdminProductRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.service.admin.auction.AuctionService; // AuctionService 인터페이스
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException; // 권한 예외
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

        // 1. AdminProduct 조회 (DTO의 productId는 AdminProduct 테이블의 productId를 참조)
        AdminProduct adminProduct = adminProductRepository.findByProductId(requestDto.getProductId())
                .orElseThrow(() -> {
                    log.warn("경매 등록 실패 - 관리자 상품 목록에 없는 Product ID: {}", requestDto.getProductId());
                    return new NoSuchElementException("관리자 상품 목록에서 해당 상품을 찾을 수 없습니다. Product ID: " + requestDto.getProductId());
                });

        // 2. 해당 AdminProduct가 이미 경매에 등록되었는지 확인
        if (adminProduct.isAuctioned()) {
            log.warn("경매 등록 실패 - Product ID {}는 이미 경매에 등록된 상품입니다 (AdminProduct ID: {}).", adminProduct.getProductId(), adminProduct.getId());
            throw new IllegalStateException("이미 경매에 등록된 상품입니다.");
        }

        // 3. 원본 Product 정보 조회 (소유권 검증 및 DTO 생성을 위해)
        Product originalProduct = productRepository.findById(adminProduct.getProductId().longValue()) // Product.id는 Long
                .orElseThrow(() -> {
                    log.error("경매 등록 실패 - AdminProduct(ID:{})에 연결된 원본 Product(ID:{})를 찾을 수 없음", adminProduct.getId(), adminProduct.getProductId());
                    return new NoSuchElementException("경매 대상 원본 상품 정보를 찾을 수 없습니다.");
                });

        // 3-1. 상품 소유권/등록 권한 검증 (주석 처리된 예시, 실제 정책에 맞게 구현 필요)
        //    - 시나리오 1: AdminProduct를 등록한 판매자만 해당 AdminProduct를 경매에 올릴 수 있는 경우
        // if (adminProduct.getPurchasedFromSellerId() == null || !adminProduct.getPurchasedFromSellerId().equals(authenticatedUserId.intValue())) {
        //     log.warn("경매 등록 실패 - Product ID {}에 대한 권한 없음 (요청자 ID: {}, AdminProduct 판매자 ID: {}). AdminProduct의 purchasedFromSellerId와 불일치",
        //             adminProduct.getProductId(), authenticatedUserId, adminProduct.getPurchasedFromSellerId());
        //     throw new AccessDeniedException("해당 상품에 대한 경매 등록 권한이 없습니다.");
        // }
        //    - 시나리오 2: 원본 Product의 판매자만 해당 Product를 경매에 올릴 수 있는 경우 (AdminProduct는 관리자만 생성 가능하다고 가정)
        // if (originalProduct.getSeller() == null || !originalProduct.getSeller().getId().equals(authenticatedUserId)) {
        //    log.warn("경매 등록 실패 - Product ID {}에 대한 권한 없음 (요청자 ID: {}, 원본 상품 판매자 ID: {}). 원본 Product의 Seller와 불일치",
        //            originalProduct.getId(), authenticatedUserId, (originalProduct.getSeller() != null ? originalProduct.getSeller().getId() : "null"));
        //    throw new AccessDeniedException("해당 상품에 대한 경매 등록 권한이 없습니다.");
        // }
        //    - 시나리오 3: 특정 역할을 가진 관리자만 모든 AdminProduct를 경매에 올릴 수 있는 경우
        //      (Spring Security의 hasRole('ADMIN') 등으로 컨트롤러에서 처리하거나, 여기서 사용자 역할 확인)

        // 3-2. 이미 해당 productId로 진행 중인 다른 Auction이 있는지 확인 (중복 방지)
        Optional<Auction> existingActiveAuction = auctionRepository.findByProductIdAndIsClosedFalse(adminProduct.getProductId());
        if (existingActiveAuction.isPresent()) {
            log.warn("경매 등록 실패 - Product ID {}에 대해 이미 진행 중인 다른 경매가 존재함", adminProduct.getProductId());
            throw new IllegalStateException("이미 해당 상품으로 진행 중인 다른 경매가 있습니다.");
        }

        // 3-3. 원본 Product의 상태가 경매 가능한 상태인지 확인
        // ProductStatus Enum에 '판매중', '품절' 등의 상태가 정의되어 있다고 가정
        if (originalProduct.getStatus() == ProductStatus.품절 || !originalProduct.isActive()) { // ProductStatus.품절 또는 다른 판매 불가 상태
            log.warn("경매 등록 실패 - Product ID {}의 상태가 부적절함 (상태: {}, 활성: {})",
                    originalProduct.getId(), originalProduct.getStatus(), originalProduct.isActive());
            throw new IllegalStateException("경매에 등록할 수 없는 상품 상태입니다.");
        }

        // 4. Auction 엔티티 생성 (AuctionCreateRequestDTO의 toEntity() 사용)
        Auction auction = requestDto.toEntity();
        // auction.setProductId(adminProduct.getProductId()); // toEntity() 내에서 requestDto.productId를 사용하므로 이미 일치

        // 5. Auction 저장
        Auction savedAuction = auctionRepository.save(auction);
        log.info("경매 등록 성공 - AuctionId: {}, ProductId: {}", savedAuction.getId(), savedAuction.getProductId());

        // 6. AdminProduct의 isAuctioned 상태 업데이트
        adminProduct.setAuctioned(true);
        adminProductRepository.save(adminProduct);
        log.info("AdminProduct (ID: {})의 isAuctioned 상태를 true로 업데이트 완료.", adminProduct.getId());

        // 7. (선택) 원본 Product의 상태를 "경매중"으로 변경
        // if (originalProduct.getStatus() != ProductStatus.경매중) { // ProductStatus Enum에 "경매중" 상태 추가 필요
        //     originalProduct.setStatus(ProductStatus.경매중);
        //     productRepository.save(originalProduct);
        //     log.info("Product (ID: {})의 상태를 '경매중'으로 변경 완료.", originalProduct.getId());
        // }

        // 8. AuctionResponseDTO 생성 및 반환
        AdminProductDTO adminProductDtoForResponse = AdminProductDTO.fromEntity(adminProduct, originalProduct);
        return AuctionResponseDTO.fromEntity(savedAuction, adminProductDtoForResponse);
    }

    @Override
    public Page<AuctionResponseDTO> getActiveAuctions(Pageable pageable, String categoryFilter, String statusFilter) {
        log.info("진행 중인 경매 목록 조회 요청 - Pageable: {}, Category: {}, Status: {}", pageable, categoryFilter, statusFilter);
        LocalDateTime now = LocalDateTime.now();

        // TODO: categoryFilter, statusFilter를 사용한 동적 쿼리 생성 로직 필요 (JPQL 또는 QueryDSL 권장)
        // 현재는 필터링 없이 진행 중인 모든 경매를 조회합니다.
        Page<Auction> auctionPage = auctionRepository.findByIsClosedFalseAndEndTimeAfter(now, pageable);

        List<Integer> productIdsFromAuctions = auctionPage.getContent().stream()
                .map(Auction::getProductId) // Auction.productId는 Integer
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> productMap = Collections.emptyMap(); // Product ID는 Long
        if (!productIdsFromAuctions.isEmpty()) {
            List<Long> productLongIds = productIdsFromAuctions.stream().map(Integer::longValue).collect(Collectors.toList());
            List<Product> products = productRepository.findAllById(productLongIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        }

        Map<Integer, AdminProduct> adminProductMap = Collections.emptyMap(); // AdminProduct.productId는 Integer
        if (!productIdsFromAuctions.isEmpty()) {
            // AdminProductRepository에 findByProductIdIn(List<Integer> productIds) 메소드가 있다고 가정
            List<AdminProduct> adminProducts = adminProductRepository.findByProductIdIn(productIdsFromAuctions);
            adminProductMap = adminProducts.stream().collect(Collectors.toMap(AdminProduct::getProductId, ap -> ap));
        }

        Map<Long, Product> finalProductMap = productMap;
        Map<Integer, AdminProduct> finalAdminProductMap = adminProductMap;

        List<AuctionResponseDTO> auctionResponseDTOs = auctionPage.getContent().stream()
                .map(auctionEntity -> {
                    Product product = finalProductMap.get(auctionEntity.getProductId().longValue()); // Product ID는 Long
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

        // Auction.productId (Integer)로 AdminProduct 조회
        AdminProduct adminProduct = adminProductRepository.findByProductId(auction.getProductId())
                .orElseThrow(() -> { // 경매는 있는데 AdminProduct가 없는 비정상적인 상황
                    log.error("경매(ID:{})에 연결된 AdminProduct(ProductID:{})를 찾을 수 없음", auctionId, auction.getProductId());
                    return new NoSuchElementException("경매에 연결된 관리자 상품 정보를 찾을 수 없습니다.");
                });

        // Auction.productId (Integer)를 Long으로 변환하여 Product 조회
        Product product = productRepository.findById(auction.getProductId().longValue())
                .orElse(null); // 원본 상품 정보가 없을 수도 있음 (데이터 정합성 이슈 고려)

        AdminProductDTO adminProductDto = AdminProductDTO.fromEntity(adminProduct, product);
        return AuctionResponseDTO.fromEntity(auction, adminProductDto);
    }
}
