package com.realive.service.review.crud;

import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.review.SellerReview;
import com.realive.domain.review.SellerReviewImage;
import com.realive.domain.seller.Seller;
import com.realive.dto.review.ReviewCreateRequestDTO;
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.dto.review.ReviewUpdateRequestDTO;
import com.realive.repository.review.crud.ReviewCRUDRepository;
import com.realive.repository.review.crud.SellerReviewImageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewCRUDServiceImpl implements ReviewCRUDService {
    private final ReviewCRUDRepository reviewRepository;
    private final SellerReviewImageRepository imageRepository;

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewCreateRequestDTO requestDTO, Long customerId) {
        // 중복 리뷰 확인
        reviewRepository.findByCustomerIdAndOrderId(customerId, requestDTO.getOrderId())
                .ifPresent(review -> {
                    throw new IllegalStateException("A review for this order already exists.");
                });

        // SellerReview 엔티티 생성
        Customer customer = new Customer();
        customer.setId(customerId);

        Order order = new Order();
        order.setId(requestDTO.getOrderId());

        Seller seller = new Seller();
        seller.setId(requestDTO.getSellerId());

        SellerReview review = SellerReview.builder()
                .customer(customer)
                .order(order)
                .seller(seller)
                .rating(requestDTO.getRating())
                .content(requestDTO.getContent())
                .build();

        // 저장
        SellerReview savedReview = reviewRepository.save(review);

        // 이미지 저장
        List<String> imageUrls = saveImages(savedReview, requestDTO.getImageUrls());

        // DTO로 변환
        return ReviewResponseDTO.builder()
                .reviewId(savedReview.getId())
                .orderId(savedReview.getOrder().getId())
                .customerId(savedReview.getCustomer().getId())
                .sellerId(savedReview.getSeller().getId())
                .productName(null) // productName은 현재 Review 엔티티에 없으므로, 필요에 따라 주문/상품 정보에서 가져오거나 null 처리
                .rating(savedReview.getRating())
                .content(savedReview.getContent())
                .imageUrls(imageUrls)
                .createdAt(savedReview.getCreatedAt())
                .isHidden(savedReview.isHidden())
                .build();
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateRequestDTO requestDTO, Long customerId) {
        // 리뷰 조회
        SellerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with ID: " + reviewId));

        // 권한 확인
        if (!review.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("You are not authorized to update this review.");
        }

        // 리뷰 업데이트
        review.setRating(requestDTO.getRating());
        review.setContent(requestDTO.getContent());

        // 기존 이미지 삭제
        imageRepository.deleteByReviewId(reviewId);

        // 새 이미지 저장
        List<String> imageUrls = saveImages(review, requestDTO.getImageUrls());

        SellerReview updatedReview = reviewRepository.save(review);

        // DTO로 변환
        return ReviewResponseDTO.builder()
                .reviewId(updatedReview.getId())
                .orderId(updatedReview.getOrder().getId())
                .customerId(updatedReview.getCustomer().getId())
                .sellerId(updatedReview.getSeller().getId())
                .productName(null) // productName은 현재 Review 엔티티에 없으므로, 필요에 따라 주문/상품 정보에서 가져오거나 null 처리
                .rating(updatedReview.getRating())
                .content(updatedReview.getContent())
                .imageUrls(imageUrls)
                .createdAt(updatedReview.getCreatedAt())
                .isHidden(updatedReview.isHidden())
                .build();
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long customerId) {
        // 리뷰 조회
        SellerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with ID: " + reviewId));

        // 권한 확인
        if (!review.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("You are not authorized to delete this review.");
        }

        // 관련 이미지 삭제
        imageRepository.deleteByReviewId(reviewId);

        // 리뷰 삭제
        reviewRepository.delete(review);
    }

    private List<String> saveImages(SellerReview review, List<String> imageUrls) {
        List<String> savedImageUrls = new ArrayList<>();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                String url = imageUrls.get(i);
                if (url != null && !url.isBlank()) {
                    SellerReviewImage reviewImage = SellerReviewImage.builder()
                            .review(review)
                            .imageUrl(url)
                            .thumbnail(i == 0) // 첫 번째 이미지를 썸네일로 설정
                            .createdAt(LocalDateTime.now())
                            .build();
                    imageRepository.save(reviewImage);
                    savedImageUrls.add(url);
                }
            }
        }
        return savedImageUrls;
    }
}