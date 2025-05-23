package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.review.SellerReview;
import com.realive.dto.admin.management.ReviewDTO;
import com.realive.repository.review.SellerReviewRepository;
import com.realive.service.admin.management.service.ReviewManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

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
public class ReviewManagementServiceImpl implements ReviewManagementService {

    private final SellerReviewRepository sellerReviewRepository;

    @Override
    public Page<ReviewDTO> getReviews(Pageable pageable) {
        return sellerReviewRepository.findAll(pageable).map(this::convertToReviewDTO);
    }

    @Override
    public Page<ReviewDTO> searchReviews(Map<String, Object> searchParams, Pageable pageable) {
        Specification<SellerReview> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // SellerReview 엔티티에 product, customer 참조가 없으므로 해당 조건으로 검색 불가.
            // Seller 참조는 있으므로 sellerName으로는 검색 가능 (SellerReview.seller.name)
            if (searchParams.get("sellerName") != null && !searchParams.get("sellerName").toString().isEmpty() && root.get("seller") != null) {
                predicates.add(cb.like(root.get("seller").get("name"), "%" + searchParams.get("sellerName").toString() + "%"));
            }
            if (searchParams.get("rating") != null && searchParams.get("rating").toString().matches("\\d+")) {
                predicates.add(cb.equal(root.get("rating"), Integer.parseInt(searchParams.get("rating").toString())));
            }
            if (searchParams.get("status") != null && !searchParams.get("status").toString().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), searchParams.get("status").toString()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return sellerReviewRepository.findAll(spec, pageable).map(this::convertToReviewDTO);
    }

    @Override
    public ReviewDTO getReviewById(Integer reviewId) {
        Optional<SellerReview> reviewOptional = sellerReviewRepository.findById(reviewId.longValue());
        SellerReview review = reviewOptional.orElseThrow(() -> new NoSuchElementException("리뷰 없음 ID: " + reviewId));
        return convertToReviewDTO(review);
    }

    @Override
    @Transactional
    public ReviewDTO updateReviewStatus(Integer reviewId, String status) {
        Optional<SellerReview> reviewOptional = sellerReviewRepository.findById(reviewId.longValue());
        SellerReview review = reviewOptional.orElseThrow(() -> new NoSuchElementException("리뷰 없음 ID: " + reviewId));
        review.setStatus(status); // SellerReview 엔티티에 status 필드가 String이라고 가정
        log.info("리뷰 ID {} 상태 업데이트: {}", reviewId, status);
        return convertToReviewDTO(sellerReviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(Integer reviewId) {
        if (!sellerReviewRepository.existsById(reviewId.longValue())) {
            throw new NoSuchElementException("삭제할 리뷰 없음 ID: " + reviewId);
        }
        sellerReviewRepository.deleteById(reviewId.longValue());
        log.info("리뷰 ID {} 삭제 완료", reviewId);
    }

    @Override
    public Page<ReviewDTO> getProductReviews(Integer productId, Pageable pageable) {
        log.warn("getProductReviews: SellerReview 엔티티에 Product 참조가 없거나, SellerReviewRepository에 관련 메소드가 없습니다.");
        // SellerReviewRepository에 findReviewsByProductId 메소드가 있고, SellerReview에 Product 참조가 있다면 주석 해제
        // return sellerReviewRepository.findReviewsByProductId(productId.longValue(), pageable)
        // .map(this::convertToReviewDTO);
        return Page.empty(pageable);
    }

    @Override
    public Page<ReviewDTO> getCustomerReviews(Integer customerId, Pageable pageable) {
        log.warn("getCustomerReviews: SellerReview 엔티티에 Customer 참조가 없거나, SellerReviewRepository에 관련 메소드가 없습니다.");
        // SellerReviewRepository에 findReviewsByCustomerId 메소드가 있고, SellerReview에 Customer 참조가 있다면 주석 해제
        // return sellerReviewRepository.findReviewsByCustomerId(customerId.longValue(), pageable)
        // .map(this::convertToReviewDTO);
        return Page.empty(pageable);
    }

    @Override
    public Page<ReviewDTO> getSellerProductReviews(Integer sellerId, Pageable pageable) {
        // 이 메소드는 SellerReview 엔티티가 Seller를 참조하고 있으므로 SellerReviewRepository.findBySellerId 사용 가능
        return sellerReviewRepository.findBySellerId(sellerId.longValue(), pageable)
                .map(this::convertToReviewDTO);
    }

    @Override
    public Map<String, Object> getReviewStatistics(Integer productId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("productId", productId);
        log.warn("getReviewStatistics: SellerReview 엔티티에 Product 참조가 없거나, SellerReviewRepository에 관련 통계 메소드가 없습니다.");
        // SellerReviewRepository에 getAverageRatingByProductId, countReviewsByProductId 메소드가 있고,
        // SellerReview 엔티티에 Product 참조가 있다면 주석 해제
        // stats.put("averageRating", sellerReviewRepository.getAverageRatingByProductId(productId.longValue()));
        // stats.put("totalReviews", sellerReviewRepository.countReviewsByProductId(productId.longValue()));
        stats.put("averageRating", 0.0); // 임시 기본값
        stats.put("totalReviews", 0L);  // 임시 기본값
        return stats;
    }

    private ReviewDTO convertToReviewDTO(SellerReview e) {
        if (e == null) return null;
        ReviewDTO.Builder builder = ReviewDTO.builder()
                .id(e.getId() != null ? e.getId().intValue() : null)
                .rating(e.getRating())
                .content(e.getContent())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt());

        // SellerReview 엔티티에 product, customer 참조가 있다면 해당 정보를 DTO에 추가
        // if (e.getProduct() != null) {
        // builder.productId(e.getProduct().getId() != null ? e.getProduct().getId().intValue() : null);
        // builder.productName(e.getProduct().getName());
        // }
        // if (e.getCustomer() != null) {
        // builder.customerId(e.getCustomer().getId() != null ? e.getCustomer().getId().intValue() : null);
        // builder.customerName(e.getCustomer().getName());
        // }
        if (e.getSeller() != null) {
            // ReviewDTO에 sellerId, sellerName 필드가 있다면 설정
            // builder.sellerId(e.getSeller().getId() != null ? e.getSeller().getId().intValue() : null);
            // builder.sellerName(e.getSeller().getName());
        }
        return builder.build();
    }
}
