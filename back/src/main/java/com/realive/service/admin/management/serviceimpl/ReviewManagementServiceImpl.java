//package com.realive.service.admin.management.serviceimpl;
//
//import com.realive.domain.review.SellerReview;
//import com.realive.dto.admin.management.ReviewDTO;
//import com.realive.repository.review.SellerReviewRepository;
//import com.realive.service.admin.management.service.ReviewManagementService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import jakarta.persistence.criteria.Predicate;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.NoSuchElementException;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class ReviewManagementServiceImpl implements ReviewManagementService {
//
//    private final SellerReviewRepository sellerReviewRepository;
//
//    @Override
//    public Page<ReviewDTO> getReviews(Pageable pageable) {
//        return sellerReviewRepository.findAll(pageable).map(this::convertToReviewDTO);
//    }
//
//    @Override
//    public Page<ReviewDTO> searchReviews(Map<String, Object> searchParams, Pageable pageable) {
//        Specification<SellerReview> spec = (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//            if (searchParams.get("productName") != null && !searchParams.get("productName").toString().isEmpty() && root.get("product") != null) {
//                predicates.add(cb.like(root.get("product").get("name"), "%" + searchParams.get("productName").toString() + "%"));
//            }
//            if (searchParams.get("customerName") != null && !searchParams.get("customerName").toString().isEmpty() && root.get("customer") != null) {
//                predicates.add(cb.like(root.get("customer").get("name"), "%" + searchParams.get("customerName").toString() + "%"));
//            }
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//        return sellerReviewRepository.findAll(spec, pageable).map(this::convertToReviewDTO);
//    }
//
//    @Override
//    public ReviewDTO getReviewById(Integer reviewId) {
//        SellerReview review = sellerReviewRepository.findById(reviewId.longValue())
//                .orElseThrow(() -> new NoSuchElementException("리뷰 없음 ID: " + reviewId));
//        return convertToReviewDTO(review);
//    }
//
//    @Override
//    @Transactional
//    public ReviewDTO updateReviewStatus(Integer reviewId, String status) {
//        SellerReview review = sellerReviewRepository.findById(reviewId.longValue())
//                .orElseThrow(() -> new NoSuchElementException("리뷰 없음 ID: " + reviewId));
//        // review.setStatus(status); // SellerReview 엔티티에 status 필드 및 로직 필요
//        log.warn("Review status update logic for review ID {} (new status: {}) needs to be implemented.", reviewId, status);
//        return convertToReviewDTO(sellerReviewRepository.save(review));
//    }
//
//    @Override
//    @Transactional
//    public void deleteReview(Integer reviewId) {
//        if (!sellerReviewRepository.existsById(reviewId.longValue())) {
//            throw new NoSuchElementException("삭제할 리뷰 없음 ID: " + reviewId);
//        }
//        sellerReviewRepository.deleteById(reviewId.longValue());
//    }
//
//    @Override
//    public Page<ReviewDTO> getProductReviews(Integer productId, Pageable pageable) {
//        return sellerReviewRepository.findReviewsByProductId(productId.longValue(), pageable)
//                .map(this::convertToReviewDTO);
//    }
//
//    @Override
//    public Page<ReviewDTO> getCustomerReviews(Integer customerId, Pageable pageable) {
//        return sellerReviewRepository.findReviewsByCustomerId(customerId.longValue(), pageable)
//                .map(this::convertToReviewDTO);
//    }
//
//    @Override
//    public Page<ReviewDTO> getSellerProductReviews(Integer sellerId, Pageable pageable) {
//        return sellerReviewRepository.findReviewsBySellerId(sellerId.longValue(), pageable)
//                .map(this::convertToReviewDTO);
//    }
//
//    @Override
//    public Map<String, Object> getReviewStatistics(Integer productId) {
//        Map<String, Object> stats = new HashMap<>();
//        stats.put("productId", productId);
//        stats.put("averageRating", sellerReviewRepository.getAverageRatingByProductId(productId.longValue()));
//        stats.put("totalReviews", sellerReviewRepository.countReviewsByProductId(productId.longValue()));
//        return stats;
//    }
//
//    private ReviewDTO convertToReviewDTO(SellerReview e) {
//        return ReviewDTO.builder().id(e.getId()).productId(e.getProduct() != null ? e.getProduct().getId() : null).productName(e.getProduct() != null ? e.getProduct().getName() : null).customerId(e.getCustomer() != null ? e.getCustomer().getId() : null).customerName(e.getCustomer() != null ? e.getCustomer().getName() : null).rating(e.getRating()).content(e.getContent()).createdAt(e.getCreatedAt()).build();
//    }
//}
