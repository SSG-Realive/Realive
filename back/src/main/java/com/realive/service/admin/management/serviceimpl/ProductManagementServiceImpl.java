package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.common.enums.ProductStatus;
import com.realive.domain.product.Product;
import com.realive.domain.review.SellerReview;
import com.realive.dto.admin.management.ProductDTO;
import com.realive.dto.admin.management.ReviewDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.review.SellerReviewRepository;
import com.realive.service.admin.logs.StatService;
import com.realive.service.admin.management.service.ProductManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductManagementServiceImpl implements ProductManagementService {

    private final ProductRepository productRepository;
    private final SellerReviewRepository sellerReviewRepository;
    private final StatService statService;

    @Override
    public Page<ProductDTO> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::convertToProductDTO);
    }

    @Override
    public Page<ProductDTO> searchProducts(Map<String, Object> searchParams, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchParams.get("name") != null && !searchParams.get("name").toString().isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + searchParams.get("name").toString() + "%"));
            }
            if (searchParams.get("status") != null && !searchParams.get("status").toString().isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("status"), ProductStatus.valueOf(searchParams.get("status").toString().toUpperCase())));
                } catch (IllegalArgumentException ex) {
                    log.warn("searchProducts: 유효하지 않은 상품 상태값 '{}'이(가) 검색 조건으로 전달되었습니다.", searchParams.get("status"));
                }
            }
            if (searchParams.get("sellerName") != null && !searchParams.get("sellerName").toString().isEmpty()) {
                predicates.add(cb.like(root.get("seller").get("name"), "%" + searchParams.get("sellerName").toString() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec, pageable).map(this::convertToProductDTO);
    }

    @Override
    public ProductDTO getProductById(Integer productId) {
        Optional<Product> productOptional = productRepository.findById(productId.longValue());
        Product product = productOptional.orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));
        return convertToProductDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO updateProductStatus(Integer productId, String statusString) {
        Optional<Product> productOptional = productRepository.findById(productId.longValue());
        Product product = productOptional.orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));
        try {
            product.setStatus(ProductStatus.valueOf(statusString.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 상품 상태값: " + statusString + ". 사용 가능: " + List.of(ProductStatus.values()));
        }
        return convertToProductDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO updateProductPrice(Integer productId, BigDecimal price) {
        Optional<Product> productOptional = productRepository.findById(productId.longValue());
        Product product = productOptional.orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        product.setPrice(price.intValue()); // Product 엔티티의 price가 int라고 가정
        return convertToProductDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO updateProductInventory(Integer productId, Integer inventory) {
        Optional<Product> productOptional = productRepository.findById(productId.longValue());
        Product product = productOptional.orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));
        if (inventory == null || inventory < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }
        product.setStock(inventory); // Product 엔티티의 stock이 int라고 가정
        return convertToProductDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO approveProduct(Integer productId, boolean approved, String message) {
        Optional<Product> productOptional = productRepository.findById(productId.longValue());
        Product product = productOptional.orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));

        // ProductStatus Enum에 SALE, REJECTED와 같은 실제 상태값 필요
        product.setStatus(approved ? ProductStatus.SALE : ProductStatus.REJECTED);
        log.info("상품 ID {} 처리. 승인: {}, 메시지: {}", productId, approved, message);
        return convertToProductDTO(productRepository.save(product));
    }

    @Override
    public Page<ReviewDTO> getProductReviews(Integer productId, Pageable pageable) {
        log.warn("getProductReviews: SellerReview 엔티티에 Product 참조가 없거나, SellerReviewRepository에 findReviewsByProductId 메소드가 없습니다.");
        // SellerReviewRepository에 Page<SellerReview> findReviewsByProductId(Long productId, Pageable pageable) 메소드가 있고,
        // SellerReview 엔티티에 Product 참조가 있다면 아래 주석 해제
        // return sellerReviewRepository.findReviewsByProductId(productId.longValue(), pageable)
        // .map(this::convertToReviewDTO);
        return Page.empty(pageable);
    }

    @Override
    public Map<String, Object> getProductStatistics(Integer productId) {
        Optional<Product> productOptional = productRepository.findById(productId.longValue());
        Product product = productOptional.orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("productId", productId);
        stats.put("productName", product.getName());

        MonthlySalesSummaryDTO monthlySummary = statService.getProductMonthlySalesSummary(productId, YearMonth.now());
        if (monthlySummary != null) {
            stats.put("thisMonthSalesAmount", monthlySummary.getTotalSalesAmount() != null ? BigDecimal.valueOf(monthlySummary.getTotalSalesAmount()) : BigDecimal.ZERO);
            stats.put("thisMonthQuantitySold", monthlySummary.getTotalQuantity() != null ? monthlySummary.getTotalQuantity().longValue() : 0L);
        } else {
            stats.put("thisMonthSalesAmount", BigDecimal.ZERO);
            stats.put("thisMonthQuantitySold", 0L);
        }

        log.warn("getProductStatistics: SellerReview 엔티티에 Product 참조가 없거나, 관련 통계 메소드가 SellerReviewRepository에 없습니다.");
        // SellerReviewRepository에 getAverageRatingByProductId, countReviewsByProductId 메소드가 있고,
        // SellerReview 엔티티에 Product 참조가 있다면 아래 주석 해제
        // stats.put("averageRating", sellerReviewRepository.getAverageRatingByProductId(productId.longValue()));
        // stats.put("totalReviews", sellerReviewRepository.countReviewsByProductId(productId.longValue()));
        stats.put("averageRating", 0.0); // 임시 기본값
        stats.put("totalReviews", 0L);  // 임시 기본값
        return stats;
    }

    private ProductDTO convertToProductDTO(Product e) {
        if (e == null) return null;
        return ProductDTO.builder()
                .id(e.getId() != null ? e.getId().intValue() : null)
                .name(e.getName())
                .price(BigDecimal.valueOf(e.getPrice()))
                .inventory(e.getStock())
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .sellerId(e.getSeller() != null && e.getSeller().getId() != null ? e.getSeller().getId().intValue() : null)
                .sellerName(e.getSeller() != null ? e.getSeller().getName() : null)
                .registeredAt(e.getCreatedAt())
                .build();
    }
    private ReviewDTO convertToReviewDTO(SellerReview e) {
        if (e == null) return null;
        ReviewDTO.Builder builder = ReviewDTO.builder()
                .id(e.getId() != null ? e.getId().intValue() : null)
                .rating(e.getRating())
                .content(e.getContent())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt());
        // SellerReview 엔티티에 product, customer 참조가 있다면 여기서 DTO에 설정
        // if (e.getProduct() != null) { ... }
        // if (e.getCustomer() != null) { ... }
        if (e.getSeller() != null) {
            // ReviewDTO에 seller 정보 필드가 있다면 설정
        }
        return builder.build();
    }
}
