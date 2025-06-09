package com.realive.serviceimpl.admin.auction;

import com.realive.domain.admin.Admin;
import com.realive.domain.auction.AdminProduct;
import com.realive.domain.auction.Auction;
import com.realive.domain.product.Product;
import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.repository.admin.AdminRepository;
import com.realive.repository.auction.AdminProductRepository;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.service.admin.auction.AuctionService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType; // JoinType 명시적 import
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final AdminRepository adminRepository; // 관리자 정보 조회를 위해 주입

    @Override
    @Transactional
    public AuctionResponseDTO registerAuction(AuctionCreateRequestDTO requestDto, Long adminUserId) {
        log.info("관리자(ID:{}) 경매 등록 요청 처리 시작 - DTO ProductId: {}", adminUserId, requestDto.getProductId());

        // 1. 관리자 존재 및 권한 확인 (예시: Admin 엔티티에 역할(role) 필드가 있다고 가정)
        Admin admin = adminRepository.findById(adminUserId.intValue()) // Admin ID가 Integer라고 가정. 실제 타입에 맞출 것.
                .orElseThrow(() -> {
                    log.warn("경매 등록 시도: 존재하지 않는 관리자 ID {}", adminUserId);
                    return new AccessDeniedException("관리자 정보를 찾을 수 없습니다. ID: " + adminUserId);
                });
        // 예시: "ROLE_AUCTION_MANAGER" 또는 "ROLE_SUPER_ADMIN" 역할을 가진 관리자만 경매 등록 가능
        // if (admin.getRoles() == null || !(admin.getRoles().contains("ROLE_AUCTION_MANAGER") || admin.getRoles().contains("ROLE_SUPER_ADMIN"))) {
        //     log.warn("관리자(ID:{}, 역할:{}) 경매 등록 권한 없음.", adminUserId, admin.getRoles());
        //     throw new AccessDeniedException("경매를 등록할 수 있는 권한이 없습니다.");
        // }
        log.info("관리자(ID:{}) 경매 등록 권한 확인됨 (예시: 기본 관리자 권한 통과).", adminUserId);

        // 2. AdminProduct 및 원본 Product 정보 조회
        AdminProduct adminProduct = adminProductRepository.findByProductId(requestDto.getProductId())
                .orElseThrow(() -> new NoSuchElementException("관리자 상품 목록에서 해당 상품을 찾을 수 없습니다. Product ID: " + requestDto.getProductId()));

        if (adminProduct.isAuctioned()) { // AdminProduct에 isAuctioned 필드가 있다고 가정
            throw new IllegalStateException("이미 경매에 등록된 상품입니다. Product ID: " + requestDto.getProductId());
        }

        Product originalProduct = productRepository.findById(adminProduct.getProductId().longValue()) // Product ID는 Long
                .orElseThrow(() -> new NoSuchElementException("경매 대상 원본 상품 정보를 찾을 수 없습니다. Product ID: " + adminProduct.getProductId()));

        // 3. 중복 활성 경매 및 원본 상품 상태 검증
        Optional<Auction> existingActiveAuction = auctionRepository.findByProductIdAndIsClosedFalse(adminProduct.getProductId());
        if (existingActiveAuction.isPresent()) {
            throw new IllegalStateException("이미 해당 상품으로 진행 중인 다른 경매가 있습니다. Product ID: " + adminProduct.getProductId());
        }

        if (!originalProduct.isActive()) { // Product에 isActive 필드가 있다고 가정
            log.warn("관리자(ID:{}) 경매 등록 실패 - Product ID {}의 상태가 비활성임.",
                    adminUserId, originalProduct.getId());
            throw new IllegalStateException("비활성 상태의 상품은 경매에 등록할 수 없습니다.");
        }

        // 4. 경매 엔티티 생성 및 저장
        Auction auction = requestDto.toEntity(); // DTO에 toEntity() 메소드가 있다고 가정
        // auction.setCreatedByAdminId(adminUserId); // Auction 엔티티에 관리자 ID 저장 필드가 있다면 설정
        Auction savedAuction = auctionRepository.save(auction);
        log.info("관리자(ID:{})에 의해 경매 등록 성공 - AuctionId: {}, ProductId: {}",
                adminUserId, savedAuction.getId(), savedAuction.getProductId());

        // 5. AdminProduct 상태 업데이트 (경매 등록됨으로 표시)
        adminProduct.setAuctioned(true); // AdminProduct에 isAuctioned 필드 업데이트
        adminProductRepository.save(adminProduct);
        log.info("AdminProduct (ID: {}, ProductId: {})의 isAuctioned 상태를 true로 업데이트 (관리자: {}).",
                adminProduct.getId(), adminProduct.getProductId(), adminUserId);

        // 6. 응답 DTO 생성
        AdminProductDTO adminProductDtoForResponse = AdminProductDTO.fromEntity(adminProduct, originalProduct); // DTO에 fromEntity 정적 메소드 가정
        return AuctionResponseDTO.fromEntity(savedAuction, adminProductDtoForResponse); // DTO에 fromEntity 정적 메소드 가정
    }

    @Override
    public Page<AuctionResponseDTO> getActiveAuctions(Pageable pageable, String categoryFilter, String statusFilter) {
        log.info("경매 목록 조회 요청 처리 - Pageable: {}, Category: {}, Status: {}", pageable, categoryFilter, statusFilter);

        Specification<Auction> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            // 카테고리 필터 (Auction -> AdminProduct -> Product 조인 후 Product.categoryName으로 필터링 가정)
            if (StringUtils.hasText(categoryFilter)) {
                // 가정: Auction 엔티티에 AdminProduct adminProduct 필드가 있고,
                // AdminProduct 엔티티에 Product product 필드가 있고,
                // Product 엔티티에 String categoryName 필드가 있음.
                // 실제 엔티티 구조에 맞게 Join 경로 및 필드명 수정 필요.
                try {
                    // N+1 문제를 피하기 위해 fetch join을 사용할 수 있으나, countQuery에서는 문제될 수 있으므로 주의
                    // query.distinct(true); // 필요시 중복 제거
                    Join<Auction, AdminProduct> adminProductJoin = root.join("adminProduct", JoinType.INNER); // Auction.adminProduct (AdminProduct 타입)
                    Join<AdminProduct, Product> productJoin = adminProductJoin.join("product", JoinType.INNER); // AdminProduct.product (Product 타입)
                    predicates.add(criteriaBuilder.equal(productJoin.get("categoryName"), categoryFilter));
                    log.debug("Applying category filter: {}", categoryFilter);
                } catch (Exception e) {
                    log.warn("카테고리 필터링 중 오류 발생 (엔티티 관계 확인 필요): {}", e.getMessage());
                    // 오류 발생 시 해당 필터는 무시하거나, 또는 에러를 던질 수 있음
                }
            }

            // 상태 필터
            if (StringUtils.hasText(statusFilter)) {
                log.debug("Applying status filter: {}", statusFilter.toUpperCase());
                switch (statusFilter.toUpperCase()) {
                    case "ON_AUCTION": // 진행 중: 시작했고, 종료되지 않았고, 마감 시간 전
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), now));
                        predicates.add(criteriaBuilder.isFalse(root.get("isClosed")));
                        predicates.add(criteriaBuilder.greaterThan(root.get("endTime"), now));
                        break;
                    case "UPCOMING": // 시작 전: 시작 시간이 미래이고, 종료되지 않음
                        predicates.add(criteriaBuilder.greaterThan(root.get("startTime"), now));
                        predicates.add(criteriaBuilder.isFalse(root.get("isClosed")));
                        break;
                    case "ENDED": // 종료됨: isClosed=true 이거나, 마감 시간이 이미 지남
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.isTrue(root.get("isClosed")),
                                criteriaBuilder.lessThanOrEqualTo(root.get("endTime"), now)
                        ));
                        break;
                    // "CLOSING_SOON" (마감 임박) 같은 더 상세한 상태는 endTime과 now를 비교하여 추가 구현 가능
                    // 예: predicates.add(criteriaBuilder.isFalse(root.get("isClosed")));
                    //     predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime"), now.plusHours(1))); // 1시간 이내 마감
                    //     predicates.add(criteriaBuilder.greaterThan(root.get("endTime"), now));
                    default:
                        log.warn("지원하지 않는 경매 상태 필터입니다: {}", statusFilter);
                        // 기본적으로는 아무 상태 필터도 적용하지 않거나, "ON_AUCTION"을 기본으로 할 수 있음
                        // 여기서는 아무 상태 필터도 적용하지 않음 (모든 상태)
                        break;
                }
            } else {
                // statusFilter가 없으면 기본적으로 "진행 중"인 경매만 조회
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), now));
                predicates.add(criteriaBuilder.isFalse(root.get("isClosed")));
                predicates.add(criteriaBuilder.greaterThan(root.get("endTime"), now));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Auction> auctionPage = auctionRepository.findAll(spec, pageable);
        List<AuctionResponseDTO> auctionResponseDTOs = convertToAuctionResponseDTOs(auctionPage.getContent());
        return new PageImpl<>(auctionResponseDTOs, pageable, auctionPage.getTotalElements());
    }

    @Override
    public AuctionResponseDTO getAuctionDetails(Integer auctionId) {
        log.info("관리자 - 경매 상세 정보 조회 요청 - AuctionId: {}", auctionId);
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + auctionId));
        // DTO 변환 로직은 convertToAuctionResponseDTOs 또는 직접 구현
        AdminProduct adminProduct = adminProductRepository.findByProductId(auction.getProductId())
                .orElseThrow(() -> new NoSuchElementException("경매에 연결된 관리자 상품 정보를 찾을 수 없습니다. Product ID: " + auction.getProductId()));
        Product product = productRepository.findById(adminProduct.getProductId().longValue()).orElse(null);
        AdminProductDTO adminProductDto = AdminProductDTO.fromEntity(adminProduct, product);
        return AuctionResponseDTO.fromEntity(auction, adminProductDto);
    }

    @Override
    public Page<AuctionResponseDTO> getAuctionsBySeller(Long sellerId, Pageable pageable) {
        log.info("관리자 - 특정 판매자(ID:{})가 등록한 경매 목록 조회 요청. Pageable: {}", sellerId, pageable);
        // AdminProduct.purchasedFromSellerId의 타입이 Integer라고 가정
        List<AdminProduct> sellerAdminProducts = adminProductRepository.findByPurchasedFromSellerId(sellerId.intValue());
        if (sellerAdminProducts.isEmpty()) {
            log.info("판매자(ID:{})에 해당하는 AdminProduct가 없습니다.", sellerId);
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

    // DTO 변환 헬퍼 메소드 (N+1 문제 최소화 노력)
    private List<AuctionResponseDTO> convertToAuctionResponseDTOs(List<Auction> auctions) {
        if (auctions == null || auctions.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. 필요한 모든 Product ID 수집
        List<Integer> auctionProductIds = auctions.stream()
                .map(Auction::getProductId)
                .distinct()
                .collect(Collectors.toList());

        // 2. Product ID로 AdminProduct 정보 일괄 조회
        Map<Integer, AdminProduct> adminProductMapByProductId = Collections.emptyMap();
        if (!auctionProductIds.isEmpty()) {
            List<AdminProduct> adminProducts = adminProductRepository.findByProductIdIn(auctionProductIds);
            adminProductMapByProductId = adminProducts.stream()
                    .collect(Collectors.toMap(AdminProduct::getProductId, ap -> ap, (ap1, ap2) -> ap1)); // 중복 키 발생 시 첫 번째 것 사용
        }

        // 3. AdminProduct에서 실제 Product ID 수집
        List<Long> originalProductIds = adminProductMapByProductId.values().stream()
                .map(adminProduct -> adminProduct.getProductId().longValue()) // AdminProduct의 productId는 Integer, Product의 ID는 Long
                .distinct()
                .collect(Collectors.toList());

        // 4. 실제 Product 정보 일괄 조회
        Map<Long, Product> productMapById = Collections.emptyMap();
        if (!originalProductIds.isEmpty()) {
            // ProductRepository에 List<Long>을 받는 findAllByIdIn 메소드가 있다고 가정
            List<Product> products = productRepository.findAllByIdIn(originalProductIds);
            productMapById = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p, (p1, p2) -> p1)); // 중복 키 발생 시 첫 번째 것 사용
        }

        // 5. 최종 DTO 변환
        Map<Integer, AdminProduct> finalAdminProductMap = adminProductMapByProductId; // final 키워드 (람다에서 사용)
        Map<Long, Product> finalProductMap = productMapById; // final 키워드

        return auctions.stream()
                .map(auctionEntity -> {
                    AdminProduct adminProduct = finalAdminProductMap.get(auctionEntity.getProductId());
                    Product product = null;
                    if (adminProduct != null) {
                        product = finalProductMap.get(adminProduct.getProductId().longValue());
                    }
                    AdminProductDTO adminProductDto = (adminProduct != null) ?
                            AdminProductDTO.fromEntity(adminProduct, product) : null;
                    return AuctionResponseDTO.fromEntity(auctionEntity, adminProductDto);
                })
                .collect(Collectors.toList());
    }
}
