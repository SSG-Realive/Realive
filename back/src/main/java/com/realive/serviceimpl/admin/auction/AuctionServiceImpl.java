package com.realive.serviceimpl.admin.auction;

import com.realive.domain.admin.Admin;
import com.realive.domain.auction.AdminProduct;
import com.realive.domain.auction.Auction;
import com.realive.domain.common.enums.AuctionStatus;
import com.realive.domain.common.enums.MediaType;
import com.realive.domain.common.enums.PaymentStatus;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderItem;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.payment.AuctionPayment;
import com.realive.domain.product.Product;
import com.realive.domain.product.ProductImage;
import com.realive.domain.customer.Customer;
import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.auction.AuctionCreateRequestDTO;
import com.realive.dto.auction.AuctionResponseDTO;
import com.realive.dto.auction.AuctionCancelResponseDTO;
import com.realive.dto.auction.AuctionUpdateRequestDTO;
import com.realive.dto.auction.AuctionWinResponseDTO;
import com.realive.dto.auction.AuctionPaymentRequestDTO;
import com.realive.dto.payment.TossPaymentApproveRequestDTO;
import com.realive.repository.admin.AdminRepository;
import com.realive.repository.auction.AdminProductRepository;
import com.realive.repository.auction.AuctionRepository;
import com.realive.repository.auction.AuctionPaymentRepository;
import com.realive.repository.product.ProductImageRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.order.OrderItemRepository;
import com.realive.repository.order.OrderDeliveryRepository;
import com.realive.service.admin.auction.AuctionService;
import com.realive.service.payment.PaymentService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
    private final AdminRepository adminRepository;
    private final ProductImageRepository productImageRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderDeliveryRepository orderDeliveryRepository;
    private final AuctionPaymentRepository auctionPaymentRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public AuctionResponseDTO registerAuction(AuctionCreateRequestDTO requestDto, Long adminUserId) {
        log.info("관리자(ID:{}) 경매 등록 요청 처리 시작 - DTO AdminProductId: {}", adminUserId, requestDto.getAdminProductId());

        // 1. 관리자 존재 및 권한 확인
        Admin admin = adminRepository.findById(adminUserId.intValue())
                .orElseThrow(() -> {
                    log.warn("경매 등록 시도: 존재하지 않는 관리자 ID {}", adminUserId);
                    return new AccessDeniedException("관리자 정보를 찾을 수 없습니다. ID: " + adminUserId);
                });

        // 2. AdminProduct 및 원본 Product 정보 조회
        AdminProduct adminProduct = adminProductRepository.findById(requestDto.getAdminProductId())
                .orElseThrow(() -> new NoSuchElementException("관리자 상품 목록에서 해당 상품을 찾을 수 없습니다. AdminProduct ID: " + requestDto.getAdminProductId()));

        // 3. 현재 진행 중인 경매 확인
        List<Auction> existingAuctions = auctionRepository.findByAdminProduct_Id(adminProduct.getId());
        for (Auction existingAuction : existingAuctions) {
            if (existingAuction.getStatus() == AuctionStatus.PROCEEDING) {
                throw new IllegalStateException("이미 해당 상품으로 진행 중인 경매가 있습니다. AdminProduct ID: " + adminProduct.getId());
            }
        }

        Product originalProduct = productRepository.findById(adminProduct.getProductId().longValue())
                .orElseThrow(() -> new NoSuchElementException("경매 대상 원본 상품 정보를 찾을 수 없습니다. Product ID: " + adminProduct.getProductId()));

        if (adminProduct.isAuctioned()) {
            log.warn("관리자(ID:{}) 경매 등록 실패 - AdminProduct ID {}는 이미 경매에 등록됨.",
                    adminUserId, adminProduct.getId());
            throw new IllegalStateException("이미 경매에 등록된 관리자 상품입니다.");
        }

        // 4. 경매 엔티티 생성 및 저장
        Auction auction = requestDto.toEntity(adminProduct);
        Auction savedAuction = auctionRepository.save(auction);
        log.info("관리자(ID:{})에 의해 경매 등록 성공 - AuctionId: {}, AdminProductId: {}",
                adminUserId, savedAuction.getId(), adminProduct.getId());

        // 5. AdminProduct 상태 업데이트
        adminProduct.setAuctioned(true);
        adminProductRepository.save(adminProduct);
        log.info("AdminProduct (ID: {}, ProductId: {})의 isAuctioned 상태를 true로 업데이트 (관리자: {}).",
                adminProduct.getId(), adminProduct.getProductId(), adminUserId);

        // 6. 응답 DTO 생성
        AdminProductDTO adminProductDtoForResponse = AdminProductDTO.fromEntity(adminProduct, originalProduct,
            productImageRepository.findFirstByProductIdAndIsThumbnailTrueAndMediaType(originalProduct.getId(), MediaType.IMAGE)
                .map(ProductImage::getUrl)
                .orElse(null));
        return AuctionResponseDTO.fromEntity(savedAuction, adminProductDtoForResponse);
    }

    @Override
    public Page<AuctionResponseDTO> getActiveAuctions(Pageable pageable, String categoryFilter, String statusFilter) {
        log.info("경매 목록 조회 요청 처리 - Pageable: {}, Category: {}, Status: {}", pageable, categoryFilter, statusFilter);

        Specification<Auction> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            // 카테고리 필터 (Auction -> AdminProduct -> Product 조인 후 Product.categoryName으로 필터링 가정)
            if (StringUtils.hasText(categoryFilter)) {
                try {
                    Join<Auction, AdminProduct> adminProductJoin = root.join("adminProduct", JoinType.INNER);
                    Join<AdminProduct, Product> productJoin = adminProductJoin.join("product", JoinType.INNER);
                    predicates.add(criteriaBuilder.equal(productJoin.get("categoryName"), categoryFilter));
                    log.debug("Applying category filter: {}", categoryFilter);
                } catch (Exception e) {
                    log.warn("카테고리 필터링 중 오류 발생 (엔티티 관계 확인 필요): {}", e.getMessage());
                }
            }

            // 상태 필터
            if (StringUtils.hasText(statusFilter)) {
                log.debug("Applying status filter: {}", statusFilter.toUpperCase());
                switch (statusFilter.toUpperCase()) {
                    case "ON_AUCTION": // 진행 중: 시작했고, 종료되지 않았고, 마감 시간 전
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), now));
                        predicates.add(criteriaBuilder.notEqual(root.get("status"), AuctionStatus.COMPLETED));
                        predicates.add(criteriaBuilder.greaterThan(root.get("endTime"), now));
                        break;
                    case "UPCOMING": // 시작 전: 시작 시간이 미래이고, 종료되지 않음
                        predicates.add(criteriaBuilder.greaterThan(root.get("startTime"), now));
                        predicates.add(criteriaBuilder.notEqual(root.get("status"), AuctionStatus.COMPLETED));
                        break;
                    case "ENDED": // 종료됨: COMPLETED 상태이거나, 마감 시간이 이미 지남
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.equal(root.get("status"), AuctionStatus.COMPLETED),
                                criteriaBuilder.lessThanOrEqualTo(root.get("endTime"), now)
                        ));
                        break;
                    default:
                        log.warn("지원하지 않는 경매 상태 필터입니다: {}", statusFilter);
                        break;
                }
            } else {
                // statusFilter가 없으면 기본적으로 "진행 중"인 경매만 조회
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), now));
                predicates.add(criteriaBuilder.notEqual(root.get("status"), AuctionStatus.COMPLETED));
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

        AdminProduct adminProduct = auction.getAdminProduct();
        Product product = productRepository.findById(adminProduct.getProductId().longValue()).orElse(null);
        AdminProductDTO adminProductDto = AdminProductDTO.fromEntity(adminProduct, product,
            productImageRepository.findFirstByProductIdAndIsThumbnailTrueAndMediaType(product.getId(), MediaType.IMAGE)
                .map(ProductImage::getUrl)
                .orElse(null));
        return AuctionResponseDTO.fromEntity(auction, adminProductDto);
    }

    @Override
    public Page<AuctionResponseDTO> getAuctionsBySeller(Long sellerId, Pageable pageable) {
        log.info("관리자 - 특정 판매자(ID:{})가 등록한 경매 목록 조회 요청. Pageable: {}", sellerId, pageable);
        
        List<AdminProduct> sellerAdminProducts = adminProductRepository.findByPurchasedFromSellerId(sellerId.intValue());
        if (sellerAdminProducts.isEmpty()) {
            log.info("판매자(ID:{})에 해당하는 AdminProduct가 없습니다.", sellerId);
            return Page.empty(pageable);
        }

        List<Integer> adminProductIds = sellerAdminProducts.stream()
                .map(AdminProduct::getId)
                .toList();
        Page<Auction> auctionPage = auctionRepository.findByAdminProduct_IdIn(adminProductIds, pageable);

        List<AuctionResponseDTO> auctionResponseDTOs = convertToAuctionResponseDTOs(auctionPage.getContent());
        return new PageImpl<>(auctionResponseDTOs, pageable, auctionPage.getTotalElements());
    }

    @Override
    public Optional<AuctionResponseDTO> getCurrentAuctionForProduct(Integer productId) {
        log.info("관리자 - 특정 상품(ID:{})에 대해 현재 진행 중인 경매 조회 요청", productId);
        
        AdminProduct adminProduct = adminProductRepository.findByProductId(productId)
                .orElseThrow(() -> new NoSuchElementException("해당 상품의 관리자 상품 정보를 찾을 수 없습니다. Product ID: " + productId));

        Optional<Auction> auctionOptional = auctionRepository.findByAdminProduct_IdAndStatusNot(adminProduct.getId(), AuctionStatus.COMPLETED);
        return auctionOptional.map(auction -> {
            Product product = productRepository.findById(adminProduct.getProductId().longValue()).orElse(null);
            AdminProductDTO adminProductDto = AdminProductDTO.fromEntity(adminProduct, product,
                productImageRepository.findFirstByProductIdAndIsThumbnailTrueAndMediaType(product.getId(), MediaType.IMAGE)
                    .map(ProductImage::getUrl)
                    .orElse(null));
            return AuctionResponseDTO.fromEntity(auction, adminProductDto);
        });
    }

    // DTO 변환 헬퍼 메소드 (N+1 문제 최소화 노력)
    private List<AuctionResponseDTO> convertToAuctionResponseDTOs(List<Auction> auctions) {
        if (auctions == null || auctions.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 필요한 모든 AdminProduct ID 수집
        List<Integer> adminProductIds = auctions.stream()
                .map(auction -> auction.getAdminProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        // 2. AdminProduct 정보 일괄 조회
        final Map<Integer, AdminProduct> adminProductMap;
        if (!adminProductIds.isEmpty()) {
            List<AdminProduct> adminProducts = adminProductRepository.findAllById(adminProductIds);
            adminProductMap = adminProducts.stream()
                    .collect(Collectors.toMap(AdminProduct::getId, ap -> ap));
        } else {
            adminProductMap = Collections.emptyMap();
        }

        // 3. AdminProduct에서 실제 Product ID 수집
        List<Long> originalProductIds = adminProductMap.values().stream()
                .map(adminProduct -> adminProduct.getProductId().longValue())
                .distinct()
                .collect(Collectors.toList());

        // 4. 실제 Product 정보 일괄 조회
        final Map<Long, Product> productMap;
        if (!originalProductIds.isEmpty()) {
            List<Product> products = productRepository.findAllById(originalProductIds);
            productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
        } else {
            productMap = Collections.emptyMap();
        }

        // 5. Product 이미지 정보 일괄 조회
        final Map<Long, String> productThumbnailMap;
        if (!originalProductIds.isEmpty()) {
            List<ProductImage> thumbnails = productImageRepository.findByProductIdInAndIsThumbnailTrueAndMediaType(
                    originalProductIds, MediaType.IMAGE);
            productThumbnailMap = thumbnails.stream()
                    .collect(Collectors.toMap(
                            pi -> pi.getProduct().getId(),
                            ProductImage::getUrl,
                            (url1, url2) -> url1 // 중복 키 발생 시 첫 번째 것 사용
                    ));
        } else {
            productThumbnailMap = Collections.emptyMap();
        }

        // 6. DTO 변환
        return auctions.stream()
                .map(auction -> {
                    AdminProduct adminProduct = adminProductMap.get(auction.getAdminProduct().getId());
                    Product product = productMap.get(adminProduct.getProductId().longValue());
                    String thumbnailUrl = productThumbnailMap.get(product.getId());
                    
                    AdminProductDTO adminProductDto = AdminProductDTO.fromEntity(adminProduct, product, thumbnailUrl);
                    return AuctionResponseDTO.fromEntity(auction, adminProductDto);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuctionCancelResponseDTO cancelAuction(Integer auctionId, Long adminUserId, String reason) {
        log.info("관리자(ID:{}) 경매 취소 요청 처리 - AuctionId: {}, 사유: {}", adminUserId, auctionId, reason);

        // 1. 경매 정보 조회
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + auctionId));

        // 2. 관리자 권한 확인
        Admin admin = adminRepository.findById(adminUserId.intValue())
                .orElseThrow(() -> new AccessDeniedException("관리자 정보를 찾을 수 없습니다. ID: " + adminUserId));

        // 3. 경매 상태 확인
        if (auction.getStatus() == AuctionStatus.COMPLETED) {
            throw new IllegalStateException("이미 종료된 경매는 취소할 수 없습니다.");
        }

        // 4. 경매 취소 처리
        auction.setStatus(AuctionStatus.CANCELLED);
        Auction savedAuction = auctionRepository.save(auction);

        // 5. AdminProduct 상태 업데이트
        AdminProduct adminProduct = savedAuction.getAdminProduct();
        adminProduct.setAuctioned(false);
        adminProductRepository.save(adminProduct);

        log.info("관리자(ID:{})에 의해 경매 취소 완료 - AuctionId: {}, AdminProductId: {}",
                adminUserId, savedAuction.getId(), adminProduct.getId());

        return AuctionCancelResponseDTO.builder()
                .auctionId(savedAuction.getId())
                .cancelled(true)
                .reason(reason)
                .cancelledAt(savedAuction.getUpdatedAt())
                .cancelledBy(admin.getId())
                .message("경매가 성공적으로 취소되었습니다.")
                .build();
    }

    @Override
    @Transactional
    public AuctionResponseDTO updateAuction(AuctionUpdateRequestDTO requestDto, Long adminUserId) {
        log.info("관리자(ID:{}) 경매 수정 요청 처리 - AuctionId: {}", adminUserId, requestDto.getId());

        // 1. 경매 정보 조회
        Auction auction = auctionRepository.findById(requestDto.getId())
                .orElseThrow(() -> new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + requestDto.getId()));

        // 2. 관리자 권한 확인
        Admin admin = adminRepository.findById(adminUserId.intValue())
                .orElseThrow(() -> new AccessDeniedException("관리자 정보를 찾을 수 없습니다. ID: " + adminUserId));

        // 3. 경매 상태 확인
        if (auction.getStatus() == AuctionStatus.COMPLETED) {
            throw new IllegalStateException("이미 종료된 경매는 수정할 수 없습니다.");
        }

        // 4. 경매 정보 업데이트
        if (requestDto.getEndTime() != null) {
            auction.setEndTime(requestDto.getEndTime());
        }
        if (requestDto.getStatus() != null) {
            auction.setStatus(requestDto.getStatus());
        }
        Auction savedAuction = auctionRepository.save(auction);

        // 5. 응답 DTO 생성
        AdminProduct adminProduct = savedAuction.getAdminProduct();
        Product product = productRepository.findById(adminProduct.getProductId().longValue()).orElse(null);
        AdminProductDTO adminProductDto = AdminProductDTO.fromEntity(adminProduct, product,
            productImageRepository.findFirstByProductIdAndIsThumbnailTrueAndMediaType(product.getId(), MediaType.IMAGE)
                .map(ProductImage::getUrl)
                .orElse(null));

        log.info("관리자(ID:{})에 의해 경매 수정 완료 - AuctionId: {}, AdminProductId: {}",
                adminUserId, savedAuction.getId(), adminProduct.getId());

        return AuctionResponseDTO.fromEntity(savedAuction, adminProductDto);
    }

    @Override
    public List<AuctionResponseDTO> getAuctionsByProductId(Integer productId) {
        log.info("상품 ID: {}에 대한 경매 목록 조회", productId);
        List<Auction> auctions = auctionRepository.findByAdminProduct_Id(productId);
        return auctions.stream()
                .map(auction -> {
                    AdminProduct adminProduct = auction.getAdminProduct();
                    Product product = productRepository.findById(adminProduct.getProductId().longValue()).orElse(null);
                    AdminProductDTO adminProductDto = AdminProductDTO.fromEntity(adminProduct, product,
                        productImageRepository.findFirstByProductIdAndIsThumbnailTrueAndMediaType(product.getId(), MediaType.IMAGE)
                            .map(ProductImage::getUrl)
                            .orElse(null));
                    return AuctionResponseDTO.fromEntity(auction, adminProductDto);
                })
                .collect(Collectors.toList());
    }

    @Override
    public AuctionWinResponseDTO getAuctionWinInfo(Integer auctionId, Long customerId) {
        log.info("낙찰 정보 조회 - AuctionId: {}, CustomerId: {}", auctionId, customerId);
        
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + auctionId));
        
        // 낙찰자 확인
        if (!customerId.equals(auction.getWinningCustomerId())) {
            throw new NoSuchElementException("해당 경매의 낙찰자가 아닙니다.");
        }
        
        // 경매가 완료되었는지 확인
        if (auction.getStatus() != AuctionStatus.COMPLETED) {
            throw new IllegalStateException("경매가 아직 완료되지 않았습니다.");
        }
        
        AdminProduct adminProduct = auction.getAdminProduct();
        Product product = productRepository.findById(adminProduct.getProductId().longValue())
                .orElseThrow(() -> new NoSuchElementException("상품 정보를 찾을 수 없습니다."));
        
        String productImageUrl = productImageRepository.findFirstByProductIdAndIsThumbnailTrueAndMediaType(product.getId(), MediaType.IMAGE)
                .map(ProductImage::getUrl)
                .orElse(null);
        
        // 결제 마감일 계산 (경매 종료 후 7일)
        LocalDateTime paymentDeadline = auction.getEndTime().plusDays(7);
        
        // 결제 상태 확인 (AuctionPayment에서 확인)
        boolean isPaid = auctionPaymentRepository.existsByCustomerIdAndAuctionIdAndStatusCompleted(customerId, auctionId);
        String paymentStatus = isPaid ? "결제완료" : "결제대기";
        
        // 낙찰 알림 로직 (결제가 완료되지 않은 경우에만 새로 낙찰된 것으로 간주)
        boolean isNewWin = !isPaid;
        String winMessage = isNewWin ? 
            "🎉 축하합니다! 경매에서 낙찰되셨습니다. 결제를 완료해주세요." : 
            "이미 결제가 완료된 상품입니다.";
        
        return AuctionWinResponseDTO.builder()
                .auctionId(auction.getId())
                .productName(product.getName())
                .productImageUrl(productImageUrl)
                .winningBidPrice(auction.getWinningBidPrice())
                .auctionEndTime(auction.getEndTime())
                .paymentDeadline(paymentDeadline)
                .isPaid(isPaid)
                .paymentStatus(paymentStatus)
                .isNewWin(isNewWin)
                .winMessage(winMessage)
                .build();
    }

    @Override
    public Page<AuctionWinResponseDTO> getWonAuctions(Long customerId, Pageable pageable) {
        log.info("낙찰한 경매 목록 조회 - CustomerId: {}", customerId);
        
        Page<Auction> wonAuctions = auctionRepository.findByWinningCustomerIdAndStatus(customerId, AuctionStatus.COMPLETED, pageable);
        
        List<AuctionWinResponseDTO> winResponseDTOs = wonAuctions.getContent().stream()
                .map(auction -> {
                    AdminProduct adminProduct = auction.getAdminProduct();
                    Product product = productRepository.findById(adminProduct.getProductId().longValue())
                            .orElseThrow(() -> new NoSuchElementException("상품 정보를 찾을 수 없습니다."));
                    
                    String productImageUrl = productImageRepository.findFirstByProductIdAndIsThumbnailTrueAndMediaType(product.getId(), MediaType.IMAGE)
                            .map(ProductImage::getUrl)
                            .orElse(null);
                    
                    LocalDateTime paymentDeadline = auction.getEndTime().plusDays(7);
                    boolean isPaid = auctionPaymentRepository.existsByCustomerIdAndAuctionIdAndStatusCompleted(customerId, auction.getId());
                    String paymentStatus = isPaid ? "결제완료" : "결제대기";
                    
                    return AuctionWinResponseDTO.builder()
                            .auctionId(auction.getId())
                            .productName(product.getName())
                            .productImageUrl(productImageUrl)
                            .winningBidPrice(auction.getWinningBidPrice())
                            .auctionEndTime(auction.getEndTime())
                            .paymentDeadline(paymentDeadline)
                            .isPaid(isPaid)
                            .paymentStatus(paymentStatus)
                            .build();
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(winResponseDTOs, pageable, wonAuctions.getTotalElements());
    }

    @Override
    @Transactional
    public Long processAuctionPayment(AuctionPaymentRequestDTO requestDto, Long customerId) {
        log.info("경매 결제 처리 시작 - AuctionId: {}, CustomerId: {}", requestDto.getAuctionId(), customerId);
        
        // 1. 경매 및 낙찰자 확인
        Auction auction = auctionRepository.findById(requestDto.getAuctionId())
                .orElseThrow(() -> new NoSuchElementException("경매 정보를 찾을 수 없습니다. ID: " + requestDto.getAuctionId()));
        
        if (!customerId.equals(auction.getWinningCustomerId())) {
            throw new NoSuchElementException("해당 경매의 낙찰자가 아닙니다.");
        }
        
        if (auction.getStatus() != AuctionStatus.COMPLETED) {
            throw new IllegalStateException("경매가 아직 완료되지 않았습니다.");
        }
        
        // 2. 이미 결제가 완료되었는지 확인
        if (auctionPaymentRepository.existsByCustomerIdAndAuctionIdAndStatusCompleted(customerId, requestDto.getAuctionId())) {
            throw new IllegalStateException("이미 결제가 완료된 경매입니다.");
        }
        
        // 3. 고객 정보 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("고객 정보를 찾을 수 없습니다. ID: " + customerId));
        
        // 4. 상품 정보 조회
        AdminProduct adminProduct = auction.getAdminProduct();
        Product product = productRepository.findById(adminProduct.getProductId().longValue())
                .orElseThrow(() -> new NoSuchElementException("상품 정보를 찾을 수 없습니다."));
        
        // 5. AuctionPayment 생성
        AuctionPayment auctionPayment = AuctionPayment.builder()
                .auctionId(auction.getId())
                .customerId(customerId)
                .paymentKey(requestDto.getPaymentKey())
                .amount(auction.getWinningBidPrice())
                .receiverName(requestDto.getReceiverName())
                .phone(requestDto.getPhone())
                .deliveryAddress(requestDto.getDeliveryAddress())
                .paymentMethod(requestDto.getPaymentMethod().name())
                .status(PaymentStatus.READY)
                .build();
        
        auctionPaymentRepository.save(auctionPayment);
        
        // 6. 토스페이먼츠 결제 승인
        TossPaymentApproveRequestDTO tossApproveRequest = TossPaymentApproveRequestDTO.builder()
                .paymentKey(requestDto.getPaymentKey())
                .orderId(requestDto.getTossOrderId())
                .amount((long) auction.getWinningBidPrice())
                .build();
        
        try {
            paymentService.approveTossPayment(tossApproveRequest);
            log.info("토스페이먼츠 결제 승인 성공 - AuctionId: {}", auction.getId());
            
            // 7. 주문 생성
            Order order = Order.builder()
                    .customer(customer)
                    .status(OrderStatus.PAYMENT_COMPLETED)
                    .totalPrice(auction.getWinningBidPrice())
                    .deliveryAddress(requestDto.getDeliveryAddress())
                    .paymentMethod(requestDto.getPaymentMethod().name())
                    .orderedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            Order savedOrder = orderRepository.save(order);
            
            // 8. 주문 아이템 생성
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(1)
                    .price(auction.getWinningBidPrice())
                    .build();
            
            orderItemRepository.save(orderItem);
            
            // 9. 배송 정보 생성
            OrderDelivery orderDelivery = OrderDelivery.builder()
                    .order(savedOrder)
                    .status(DeliveryStatus.DELIVERY_PREPARING)
                    .startDate(LocalDateTime.now())
                    .build();
            
            orderDeliveryRepository.save(orderDelivery);
            
            // 10. AuctionPayment 상태 업데이트
            auctionPayment.setStatus(PaymentStatus.COMPLETED);
            auctionPayment.setPaidAt(LocalDateTime.now());
            auctionPayment.setOrderId(savedOrder.getId());
            auctionPaymentRepository.save(auctionPayment);
            
            log.info("경매 결제 처리 완료 - OrderId: {}, AuctionId: {}", savedOrder.getId(), auction.getId());
            return savedOrder.getId();
            
        } catch (Exception e) {
            log.error("경매 결제 처리 실패 - AuctionId: {}", auction.getId(), e);
            throw new RuntimeException("결제 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
