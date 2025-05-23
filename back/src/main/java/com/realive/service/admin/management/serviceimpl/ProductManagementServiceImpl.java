package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.common.enums.ProductStatus;
import com.realive.domain.product.Product;
import com.realive.domain.review.SellerReview;
import com.realive.dto.admin.management.ProductDTO;
import com.realive.dto.admin.management.ReviewDTO;
import com.realive.dto.logs.salessum.DailySalesSummaryDTO; // StatService DTO
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO; // StatService DTO
import com.realive.repository.product.ProductRepository;
import com.realive.repository.review.SellerReviewRepository;
import com.realive.service.admin.logs.StatService; // StatService 주입
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductManagementServiceImpl implements ProductManagementService {

    private final ProductRepository productRepository;
    private final SellerReviewRepository sellerReviewRepository;
    private final StatService statService; // 상품 판매 통계용

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
                } catch (IllegalArgumentException ex) { /* 무시 */ }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec, pageable).map(this::convertToProductDTO);
    }

    @Override
    public ProductDTO getProductById(Integer productId) {
        Product product = productRepository.findById(productId.longValue())
                .orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));
        return convertToProductDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO updateProductStatus(Integer productId, String statusString) {
        Product product = productRepository.findById(productId.longValue())
                .orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));
        try {
            product.setStatus(ProductStatus.valueOf(statusString.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 상품 상태값: " + statusString);
        }
        return convertToProductDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO updateProductPrice(Integer productId, BigDecimal price) {
        Product product = productRepository.findById(productId.longValue())
                .orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        product.setPrice(price.intValue());
        return convertToProductDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO updateProductInventory(Integer productId, Integer inventory) {
        Product product = productRepository.findById(productId.longValue())
                .orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));
        if (inventory == null || inventory < 0) throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        product.setStock(inventory);
        return convertToProductDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO approveProduct(Integer productId, boolean approved, String message) {
        Product product = productRepository.findById(productId.longValue())
                .orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));
        product.setStatus(approved ? ProductStatus.SALE : ProductStatus.REJECTED);
        log.info("상품 ID {} 처리. 승인: {}, 메시지: {}", productId, approved, message);
        return convertToProductDTO(productRepository.save(product));
    }

    @Override
    public Page<ReviewDTO> getProductReviews(Integer productId, Pageable pageable) {
        return sellerReviewRepository.findReviewsByProductId(productId.longValue(), pageable)
                .map(this::convertToReviewDTO);
    }

    @Override
    public Map<String, Object> getProductStatistics(Integer productId) {
        log.info("상품 통계 조회 (StatService 활용) - ID: {}", productId);
        Product product = productRepository.findById(productId.longValue())
                .orElseThrow(() -> new NoSuchElementException("상품 없음 ID: " + productId));
        Map<String, Object> stats = new HashMap<>();
        stats.put("productId", product.getId());
        stats.put("productName", product.getName());

        // StatService를 사용하여 상품별 판매 통계 조회
        MonthlySalesSummaryDTO monthlySummary = statService.getProductMonthlySalesSummary(productId, YearMonth.now()); // 이번 달 통계 예시
        if (monthlySummary != null) {
            stats.put("thisMonthSalesAmount", monthlySummary.getTotalSalesAmount() != null ? BigDecimal.valueOf(monthlySummary.getTotalSalesAmount()) : BigDecimal.ZERO);
            stats.put("thisMonthQuantitySold", monthlySummary.getTotalQuantity() != null ? monthlySummary.getTotalQuantity() : 0L);
        } else {
            stats.put("thisMonthSalesAmount", BigDecimal.ZERO);
            stats.put("thisMonthQuantitySold", 0L);
        }
        // 전체 기간 판매량 등도 StatService에 기능 추가 후 호출 가능

        stats.put("averageRating", sellerReviewRepository.getAverageRatingByProductId(product.getId()));
        stats.put("totalReviews", sellerReviewRepository.countReviewsByProductId(product.getId()));
        return stats;
    }

    private ProductDTO convertToProductDTO(Product e) {
        return ProductDTO.builder().id(e.getId()).name(e.getName()).price(BigDecimal.valueOf(e.getPrice())).inventory(e.getStock()).status(e.getStatus() != null ? e.getStatus().name() : null).sellerId(e.getSeller() != null ? e.getSeller().getId() : null).sellerName(e.getSeller() != null ? e.getSeller().getName() : null).registeredAt(e.getCreatedAt()).build();
    }
    private ReviewDTO convertToReviewDTO(SellerReview e) {
        return ReviewDTO.builder().id(e.getId()).productId(e.getProduct() != null ? e.getProduct().getId() : null).productName(e.getProduct() != null ? e.getProduct().getName() : null).customerId(e.getCustomer() != null ? e.getCustomer().getId() : null).customerName(e.getCustomer() != null ? e.getCustomer().getName() : null).rating(e.getRating()).content(e.getContent()).createdAt(e.getCreatedAt()).build();
    }
}
