package com.realive.service.admin.management.serviceimpl;


import com.realive.dto.admin.management.ProductDTO;
import com.realive.dto.admin.management.ReviewDTO;
import com.realive.repository.ProductRepository;
import com.realive.repository.ReviewRepository;
import com.realive.service.admin.management.proman.ProductManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductManagementServiceImpl implements ProductManagementService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public Page<ProductDTO> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<ProductDTO> searchProducts(Map<String, Object> searchParams, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (searchParams.containsKey("name")) {
            String name = (String) searchParams.get("name");
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("name"), "%" + name + "%"));
        }

        if (searchParams.containsKey("status")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), searchParams.get("status")));
        }

        if (searchParams.containsKey("sellerId")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("seller").get("id"), searchParams.get("sellerId")));
        }

        if (searchParams.containsKey("minPrice") && searchParams.containsKey("maxPrice")) {
            BigDecimal minPrice = (BigDecimal) searchParams.get("minPrice");
            BigDecimal maxPrice = (BigDecimal) searchParams.get("maxPrice");
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("price"), minPrice, maxPrice));
        }

        return productRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public ProductDTO getProductById(Integer productId) {
        return productRepository.findById(productId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new NoSuchElementException("상품 ID가 존재하지 않습니다: " + productId));
    }

    @Override
    @Transactional
    public ProductDTO updateProductStatus(Integer productId, String status) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품 ID가 존재하지 않습니다: " + productId));

        product.setStatus(status);
        return convertToDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO updateProductPrice(Integer productId, BigDecimal price) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품 ID가 존재하지 않습니다: " + productId));

        product.setPrice(price);
        return convertToDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO updateProductInventory(Integer productId, Integer inventory) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품 ID가 존재하지 않습니다: " + productId));

        product.setInventory(inventory);
        return convertToDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO approveProduct(Integer productId, boolean approved, String message) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품 ID가 존재하지 않습니다: " + productId));

        if (approved) {
            product.setStatus("APPROVED");
        } else {
            product.setStatus("REJECTED");
            product.setRejectionReason(message);
        }

        return convertToDTO(productRepository.save(product));
    }

    @Override
    public Page<ReviewDTO> getProductReviews(Integer productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(this::convertToReviewDTO);
    }

    @Override
    public Map<String, Object> getProductStatistics(Integer productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품 ID가 존재하지 않습니다: " + productId));

        // 판매량
        Integer totalSales = productRepository.getTotalSalesCountByProductId(productId);

        // 매출액
        BigDecimal totalRevenue = productRepository.getTotalRevenueByProductId(productId);

        // 평균 평점
        Double averageRating = reviewRepository.getAverageRatingByProductId(productId);

        // 리뷰 개수
        Long reviewCount = reviewRepository.countByProductId(productId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSales", totalSales != null ? totalSales : 0);
        statistics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        statistics.put("averageRating", averageRating != null ? averageRating : 0.0);
        statistics.put("reviewCount", reviewCount);

        return statistics;
    }

    // 엔티티 -> DTO 변환 메소드
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSellerId(product.getSeller().getId());
        dto.setSellerName(product.getSeller().getName());
        dto.setStatus(product.getStatus());
        dto.setPrice(product.getPrice());
        dto.setInventory(product.getInventory());
        dto.setRegisteredAt(product.getRegisteredAt());
        return dto;
    }

    private ReviewDTO convertToReviewDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setCustomerId(review.getCustomer().getId());
        dto.setCustomerName(review.getCustomer().getName());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setStatus(review.getStatus());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
