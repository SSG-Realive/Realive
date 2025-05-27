package com.realive.serviceimpl.admin.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.auction.AdminProduct;
import com.realive.domain.product.Product;
<<<<<<< HEAD
=======
// import com.realive.domain.common.enums.ProductStatus; // ProductStatus를 직접적인 조건으로 사용하지 않음
>>>>>>> 8959463cf51242389515b4c0a5b0de74a469d25e
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
<<<<<<< HEAD
import org.springframework.security.access.AccessDeniedException; // AccessDeniedException import 확인
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.util.StringUtils; // getActiveAuctions에서 필터링 시 필요
=======
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
>>>>>>> 8959463cf51242389515b4c0a5b0de74a469d25e

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
<<<<<<< HEAD
    public AuctionResponseDTO registerAuction(AuctionCreateRequestDTO requestDto, Long adminUserId) { // 파라미터명 변경됨
        log.info("관리자(ID:{}) 경매 등록 요청 - DTO ProductId: {}", adminUserId, requestDto.getProductId());

        // 1. AdminProduct 및 원본 Product 정보 조회 (기존 로직과 유사)
=======
    public AuctionResponseDTO registerAuction(AuctionCreateRequestDTO requestDto, Long authenticatedUserId) {
        log.info("경매 등록 요청 - DTO ProductId: {}, RequestingUserId: {}", requestDto.getProductId(), authenticatedUserId);

>>>>>>> 8959463cf51242389515b4c0a5b0de74a469d25e
        AdminProduct adminProduct = adminProductRepository.findByProductId(requestDto.getProductId())
                .orElseThrow(() -> new NoSuchElementException("관리자 상품 목록에서 해당 상품을 찾을 수 없습니다. Product ID: " + requestDto.getProductId()));

        if (adminProduct.isAuctioned()) {
            throw new IllegalStateException("이미 경매에 등록된 상품입니다.");
        }

        Product originalProduct = productRepository.findById(adminProduct.getProductId().longValue())
                .orElseThrow(() -> new NoSuchElementException("경매 대상 원본 상품 정보를 찾을 수 없습니다."));

<<<<<<< HEAD
        // 2. 권한 검증 (관리자 시나리오)
        //    - adminUserId가 유효한 관리자인지는 Spring Security에서 처리되었다고 가정.
        //    - 여기서는 관리자가 어떤 상품에 대해 경매를 등록할 수 있는지에 대한 정책을 구현.
        //      예시: 관리자는 isAuctioned=false이고 isActive=true인 모든 상품에 대해 경매 등록 가능.
        //      또는, 특정 관리자 등급만 가능하다거나, 특정 카테고리 상품만 가능하다는 등의 복잡한 규칙이 있을 수 있음.
        //      가장 기본적인 형태는, "관리자이기만 하면 특정 조건의 상품에 대해 등록 가능"으로 가정.

        //    아래는 '관리자는 어떤 상품이든 등록할 수 있다'는 전제하에, 기존 판매자 ID 비교 로직을 제거하거나 주석 처리.
        //    만약 관리자가 특정 판매자를 "대신하여" 등록하고, 그 판매자 정보가 DTO에 있다면 다른 검증 필요.
        /*
        if (adminProduct.getPurchasedFromSellerId() == null || !adminProduct.getPurchasedFromSellerId().equals(adminUserId.intValue())) {
            // 이 로직은 이제 adminUserId가 관리자 ID이므로 직접적으로 맞지 않음.
            // 만약 requestDto에 sellerId가 포함되어 있고, 관리자가 그 sellerId를 대신하여 등록한다면
            // 그 sellerId와 adminProduct.getPurchasedFromSellerId()를 비교해야 함.
            // 여기서는 관리자는 상품 소유권과 무관하게 등록 가능하다고 가정하고 이 검증 생략.
            log.warn("관리자(ID:{})가 Product ID {}에 대한 경매 등록 시 권한 검증 실패 (이 부분은 관리자 정책에 맞게 수정 필요).",
                    adminUserId, adminProduct.getProductId());
            throw new AccessDeniedException("관리자가 해당 상품에 대한 경매를 등록할 권한이 없습니다 (정책 재검토 필요).");
        }
        */
        log.info("관리자(ID:{})가 Product ID {}에 대한 경매 등록 권한 검증 통과 (기본 정책: 관리자는 등록 가능).",
                adminUserId, adminProduct.getProductId());


        // 3. 중복 경매 및 상품 상태 검증 (기존 로직과 유사)
=======
        // --- 권한 검증 로직 (실제 정책에 맞게 구현) ---
        // 예시: AdminProduct의 purchasedFromSellerId와 authenticatedUserId가 일치해야 함
        // if (adminProduct.getPurchasedFromSellerId() == null || !adminProduct.getPurchasedFromSellerId().equals(authenticatedUserId.intValue())) {
        //     throw new AccessDeniedException("해당 상품에 대한 경매 등록 권한이 없습니다.");
        // }
        // --- 권한 검증 로직 끝 ---

>>>>>>> 8959463cf51242389515b4c0a5b0de74a469d25e
        Optional<Auction> existingActiveAuction = auctionRepository.findByProductIdAndIsClosedFalse(adminProduct.getProductId());
        if (existingActiveAuction.isPresent()) {
            throw new IllegalStateException("이미 해당 상품으로 진행 중인 다른 경매가 있습니다.");
        }

<<<<<<< HEAD
        if (!originalProduct.isActive()) {
            log.warn("관리자(ID:{}) 경매 등록 실패 - Product ID {}의 상태가 부적절함 (활성: {})",
                    adminUserId, originalProduct.getId(), originalProduct.isActive());
            throw new IllegalStateException("경매에 등록할 수 없는 상품 상태입니다 (비활성).");
        }

        // 4. 경매 생성 및 저장 (기존 로직과 유사)
        Auction auction = requestDto.toEntity();
        // Auction 엔티티에 createdByAdminId 같은 필드가 있다면 여기서 설정:
        // auction.setCreatedByAdminId(adminUserId);
        Auction savedAuction = auctionRepository.save(auction);
        log.info("관리자(ID:{})에 의해 경매 등록 성공 - AuctionId: {}, ProductId: {}",
                adminUserId, savedAuction.getId(), savedAuction.getProductId());

        adminProduct.setAuctioned(true);
        adminProductRepository.save(adminProduct);
        log.info("AdminProduct (ID: {})의 isAuctioned 상태를 true로 업데이트 완료 (관리자: {}).",
                adminProduct.getId(), adminUserId);
=======
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
>>>>>>> 8959463cf51242389515b4c0a5b0de74a469d25e

        AdminProductDTO adminProductDtoForResponse = AdminProductDTO.fromEntity(adminProduct, originalProduct);
        return AuctionResponseDTO.fromEntity(savedAuction, adminProductDtoForResponse);
    }

    @Override
    public Page<AuctionResponseDTO> getActiveAuctions(Pageable pageable, String categoryFilter, String statusFilter) {
<<<<<<< HEAD
        // 이 메소드는 관리자가 호출. 필터링 로직은 여전히 TODO.
        log.info("관리자 - 진행 중인 경매 목록 조회 요청 - Pageable: {}, Category: {}, Status: {}", pageable, categoryFilter, statusFilter);
        LocalDateTime now = LocalDateTime.now();
        Page<Auction> auctionPage = auctionRepository.findByIsClosedFalseAndEndTimeAfter(now, pageable);
        List<AuctionResponseDTO> auctionResponseDTOs = convertToAuctionResponseDTOs(auctionPage.getContent());
=======
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

>>>>>>> 8959463cf51242389515b4c0a5b0de74a469d25e
        return new PageImpl<>(auctionResponseDTOs, pageable, auctionPage.getTotalElements());
    }

    @Override
    public AuctionResponseDTO getAuctionDetails(Integer auctionId) {
<<<<<<< HEAD
        // 이 메소드는 관리자가 호출.
        log.info("관리자 - 경매 상세 정보 조회 요청 - AuctionId: {}", auctionId);
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("경매를 찾을 수 없습니다. ID: " + auctionId));
        // ... (이하 DTO 변환 로직은 이전과 동일)
        AdminProduct adminProduct = adminProductRepository.findByProductId(auction.getProductId())
                .orElseThrow(() -> new NoSuchElementException("경매에 연결된 관리자 상품 정보를 찾을 수 없습니다. Product ID: " + auction.getProductId()));
        Product product = productRepository.findById(auction.getProductId().longValue()).orElse(null);
        AdminProductDTO adminProductDto = AdminProductDTO.fromEntity(adminProduct, product);
        return AuctionResponseDTO.fromEntity(auction, adminProductDto);
    }

    @Override
    public Page<AuctionResponseDTO> getAuctionsBySeller(Long sellerId, Pageable pageable) {
        // 이 메소드는 관리자가 특정 판매자의 경매를 조회.
        log.info("관리자 - 특정 판매자(ID:{})가 등록한 경매 목록 조회 요청. Pageable: {}", sellerId, pageable);
        List<AdminProduct> sellerAdminProducts = adminProductRepository.findByPurchasedFromSellerId(sellerId.intValue());
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
        Page<Auction> auctionPage = auctionRepository.findByProductIdIn(productIdsBySeller, pageable);
        List<AuctionResponseDTO> auctionResponseDTOs = convertToAuctionResponseDTOs(auctionPage.getContent());
        return new PageImpl<>(auctionResponseDTOs, pageable, auctionPage.getTotalElements());
    }

    @Override
    public Optional<AuctionResponseDTO> getCurrentAuctionForProduct(Integer productId) {
        // 이 메소드는 관리자가 특정 상품의 현재 경매를 조회.
        log.info("관리자 - 특정 상품(ID:{})에 대해 현재 진행 중인 경매 조회 요청", productId);
        Optional<Auction> auctionOptional = auctionRepository.findByProductIdAndIsClosedFalse(productId);
        return auctionOptional.map(auction -> {
            AdminProduct adminProduct = adminProductRepository.findByProductId(auction.getProductId()).orElse(null);
            Product product = null;
            if (adminProduct != null) {
                product = productRepository.findById(adminProduct.getProductId().longValue()).orElse(null);
            }
            AdminProductDTO adminProductDto = (adminProduct != null) ? AdminProductDTO.fromEntity(adminProduct, product) : null;
            return AuctionResponseDTO.fromEntity(auction, adminProductDto);
        });
    }

    // DTO 변환 헬퍼 메소드 (기존과 동일)
    private List<AuctionResponseDTO> convertToAuctionResponseDTOs(List<Auction> auctions) {
        // ... (이전 답변의 코드와 동일)
        if (auctions == null || auctions.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> productIds = auctions.stream().map(Auction::getProductId).distinct().collect(Collectors.toList());
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
        return auctions.stream().map(auctionEntity -> {
            Product product = finalProductMap.get(auctionEntity.getProductId().longValue());
            AdminProduct adminProduct = finalAdminProductMap.get(auctionEntity.getProductId());
            AdminProductDTO adminProductDto = (adminProduct != null) ? AdminProductDTO.fromEntity(adminProduct, product) : null;
            return AuctionResponseDTO.fromEntity(auctionEntity, adminProductDto);
        }).collect(Collectors.toList());
    }
=======
        log.info("경매 상세 정보 조회 요청 - AuctionId: {}", auctionId);
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("경매를 찾을 수 없습니다. ID: " + auctionId));

        AdminProduct adminProduct = adminProductRepository.findByProductId(auction.getProductId())
                .orElseThrow(() -> new NoSuchElementException("경매에 연결된 관리자 상품 정보를 찾을 수 없습니다. Product ID: " + auction.getProductId()));

        Product product = productRepository.findById(auction.getProductId().longValue()).orElse(null);

        AdminProductDTO adminProductDto = AdminProductDTO.fromEntity(adminProduct, product);
        return AuctionResponseDTO.fromEntity(auction, adminProductDto);
    }
>>>>>>> 8959463cf51242389515b4c0a5b0de74a469d25e
}
