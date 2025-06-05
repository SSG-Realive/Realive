package com.realive.serviceimpl.admin.product;

import com.realive.domain.admin.Admin;
import com.realive.domain.auction.AdminProduct;
import com.realive.domain.common.enums.MediaType;
import com.realive.domain.product.Product;
import com.realive.domain.seller.Seller;
import com.realive.dto.admin.ProductDetailDTO;
import com.realive.dto.auction.AdminPurchaseRequestDTO;
import com.realive.dto.auction.AdminProductDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductSearchCondition;
import com.realive.repository.admin.AdminRepository;
import com.realive.repository.auction.AdminProductRepository;
import com.realive.repository.product.ProductImageRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.service.admin.product.AdminProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductServiceImpl implements AdminProductService {

    private final AdminProductRepository adminProductRepository;
    private final ProductRepository productRepository;
    private final AdminRepository adminRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    @Transactional
    public AdminProductDTO purchaseProduct(AdminPurchaseRequestDTO requestDTO, Integer adminId) {
        log.info("관리자 상품 매입 처리 시작: adminId={}, productId={}, price={}", 
            adminId, requestDTO.getProductId(), requestDTO.getPurchasePrice());

        // 1. 관리자 존재 확인
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 관리자입니다."));

        // 2. 상품 존재 여부 확인
        Product product = productRepository.findById(requestDTO.getProductId().longValue())
            .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        // 3. 상품 상태 확인
        if (!product.isActive()) {
            throw new IllegalStateException("비활성화된 상품은 매입할 수 없습니다.");
        }

        // 4. 상품 수량 확인
        if (product.getStock() <= 0) {
            throw new IllegalStateException("재고가 없는 상품은 매입할 수 없습니다.");
        }

        // 5. 판매자 확인
        Seller seller = product.getSeller();
        if (seller == null) {
            throw new IllegalStateException("판매자 정보가 없는 상품입니다.");
        }

        // 6. 이미 매입된 상품인지 확인
        if (adminProductRepository.findByProductId(requestDTO.getProductId()).isPresent()) {
            throw new IllegalStateException("이미 매입된 상품입니다.");
        }

        // 7. 판매자의 상품 수량 감소
        product.setStock(product.getStock() - 1);
        productRepository.save(product);

        // 8. AdminProduct 생성
        AdminProduct adminProduct = AdminProduct.builder()
            .productId(requestDTO.getProductId())
            .purchasePrice(requestDTO.getPurchasePrice())
            .purchasedFromSellerId(seller.getId().intValue())
            .purchasedAt(LocalDateTime.now())
            .isAuctioned(false)
            .build();

        // 9. AdminProduct 저장
        AdminProduct savedAdminProduct = adminProductRepository.save(adminProduct);
        log.info("관리자 상품 매입 완료: adminProductId={}", savedAdminProduct.getId());

        return AdminProductDTO.fromEntity(savedAdminProduct, product);
    }

    @Override
    public AdminProductDTO getAdminProduct(Integer productId) {
        AdminProduct adminProduct = adminProductRepository.findByProductId(productId)
            .orElseThrow(() -> new NoSuchElementException("관리자 상품을 찾을 수 없습니다."));

        Product product = productRepository.findById(productId.longValue())
                .orElse(null);

        return AdminProductDTO.fromEntity(adminProduct, product);
    }

    @Override
    public Page<AdminProductDTO> getAllAdminProducts(Pageable pageable, String categoryFilter, Boolean isAuctioned) {
        log.info("관리자 물품 목록 조회 요청 - Pageable: {}, Category: {}, IsAuctioned: {}", 
                pageable, categoryFilter, isAuctioned);

        Specification<AdminProduct> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 카테고리 필터
            if (StringUtils.hasText(categoryFilter)) {
                try {
                    Join<AdminProduct, Product> productJoin = root.join("product", JoinType.INNER);
                    predicates.add(criteriaBuilder.equal(productJoin.get("categoryName"), categoryFilter));
                    log.debug("Applying category filter: {}", categoryFilter);
                } catch (Exception e) {
                    log.warn("카테고리 필터링 중 오류 발생: {}", e.getMessage());
                }
            }

            // 경매 등록 여부 필터
            if (isAuctioned != null) {
                predicates.add(criteriaBuilder.equal(root.get("auctioned"), isAuctioned));
                log.debug("Applying auctioned filter: {}", isAuctioned);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<AdminProduct> adminProductPage = adminProductRepository.findAll(spec, pageable);
        List<AdminProductDTO> adminProductDTOs = convertToAdminProductDTOs(adminProductPage.getContent());
        return new PageImpl<>(adminProductDTOs, pageable, adminProductPage.getTotalElements());
    }

    @Override
    public Optional<AdminProductDTO> getAdminProductDetails(Integer adminProductId) {
        log.info("관리자 물품 상세 정보 조회 요청 - AdminProductId: {}", adminProductId);
        
        return adminProductRepository.findById(adminProductId)
                .map(adminProduct -> {
                    Product product = productRepository.findById(adminProduct.getProductId().longValue()).orElse(null);
                    return AdminProductDTO.fromEntity(adminProduct, product);
                });
    }

    @Override
    public PageResponseDTO<ProductListDTO> getAdminProducts(ProductSearchCondition condition) {
        log.info("관리자 물품 목록 조회 - 조건: {}", condition);
        
        // 1. AdminProduct 목록 조회 (페이징 없이)
        Specification<AdminProduct> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 카테고리 필터
            if (condition.getCategoryId() != null) {
                Join<AdminProduct, Product> productJoin = root.join("product", JoinType.INNER);
                predicates.add(cb.equal(productJoin.get("category").get("id"), condition.getCategoryId()));
            }

            // 상태 필터
            if (condition.getStatus() != null) {
                Join<AdminProduct, Product> productJoin = root.join("product", JoinType.INNER);
                predicates.add(cb.equal(productJoin.get("status"), condition.getStatus()));
            }

            // 활성화 여부 필터
            if (condition.getIsActive() != null) {
                Join<AdminProduct, Product> productJoin = root.join("product", JoinType.INNER);
                predicates.add(cb.equal(productJoin.get("active"), condition.getIsActive()));
            }

            // 가격 범위 필터
            if (condition.getMinPrice() != null) {
                Join<AdminProduct, Product> productJoin = root.join("product", JoinType.INNER);
                predicates.add(cb.greaterThanOrEqualTo(productJoin.get("price"), condition.getMinPrice()));
            }
            if (condition.getMaxPrice() != null) {
                Join<AdminProduct, Product> productJoin = root.join("product", JoinType.INNER);
                predicates.add(cb.lessThanOrEqualTo(productJoin.get("price"), condition.getMaxPrice()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 2. AdminProduct ID 목록 조회
        List<AdminProduct> adminProducts = adminProductRepository.findAll(spec);
        List<Long> productIds = adminProducts.stream()
                .map(adminProduct -> adminProduct.getProductId().longValue())
                .toList();

        if (productIds.isEmpty()) {
            return PageResponseDTO.<ProductListDTO>withAll()
                    .pageRequestDTO(condition)
                    .dtoList(new ArrayList<>())
                    .total(0)
                    .build();
        }

        // 3. Product 정보 조회
        List<Product> allProducts = productRepository.findAllByIdIn(productIds);
        
        // 4. createdAt 기준으로 정렬
        allProducts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
        
        // 5. 페이징 처리
        int start = (condition.getPage() - 1) * condition.getSize();
        int end = Math.min(start + condition.getSize(), allProducts.size());
        List<Product> products = allProducts.subList(start, end);

        // 6. 썸네일 이미지 매핑
        List<Object[]> rows = productImageRepository.findThumbnailUrlsByProductIds(
            products.stream().map(Product::getId).toList(), 
            MediaType.IMAGE
        );
        Map<Long, String> imageMap = rows.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (String) row[1]
                ));

        // 7. DTO 변환
        List<ProductListDTO> dtoList = products.stream()
                .map(product -> ProductListDTO.from(product, imageMap.get(product.getId())))
                .collect(Collectors.toList());

        return PageResponseDTO.<ProductListDTO>withAll()
                .pageRequestDTO(condition)
                .dtoList(dtoList)
                .total(allProducts.size())
                .build();
    }

    // DTO 변환 헬퍼 메소드
    private List<AdminProductDTO> convertToAdminProductDTOs(List<AdminProduct> adminProducts) {
        if (adminProducts == null || adminProducts.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 필요한 모든 Product ID 수집
        List<Long> productIds = adminProducts.stream()
                .map(adminProduct -> adminProduct.getProductId().longValue())
                .distinct()
                .collect(Collectors.toList());

        // 2. Product 정보 일괄 조회
        Map<Long, Product> productMap = productRepository.findAllByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (p1, p2) -> p1));

        // 3. DTO 변환
        return adminProducts.stream()
                .map(adminProduct -> {
                    Product product = productMap.get(adminProduct.getProductId().longValue());
                    return AdminProductDTO.fromEntity(adminProduct, product);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProductDetailDTO getProductDetails(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return ProductDetailDTO.from(product, null);
    }

    @Override
    @Transactional
    public void deactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }
} 