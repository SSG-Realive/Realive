package com.realive.service.review.crud;

import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.review.SellerReview;
import com.realive.domain.review.SellerReviewImage;
import com.realive.domain.seller.Seller;
import com.realive.dto.review.ReviewCreateRequestDTO;
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.dto.review.ReviewUpdateRequestDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.review.crud.ReviewCRUDRepository;
import com.realive.repository.review.crud.SellerReviewImageRepository;
import com.realive.repository.seller.SellerRepository;
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
    private final CustomerRepository customerRepository; // 주입
    private final OrderRepository orderRepository;       // 주입
    private final SellerRepository sellerRepository;     // 주입

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewCreateRequestDTO requestDTO, Long customerId) {
        // 1. 중복 리뷰 확인 (orderId, customerId, sellerId 조합으로 확인)
        reviewRepository.findByOrderIdAndCustomerIdAndSellerId(requestDTO.getOrderId(), customerId, requestDTO.getSellerId())
                .ifPresent(review -> {
                    throw new IllegalStateException("A review for this order and seller by this customer already exists.");
                });

        // 2. Customer, Order, Seller 엔티티 조회 (실제 존재하는지 확인)
        // 이 부분이 중요하게 변경됩니다.
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + customerId));
        Order order = orderRepository.findById(requestDTO.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + requestDTO.getOrderId()));
        Seller seller = sellerRepository.findById(requestDTO.getSellerId())
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with ID: " + requestDTO.getSellerId()));

        // 3. SellerReview 엔티티 생성
        SellerReview review = SellerReview.builder()
                .customer(customer) // 조회된 실제 엔티티 사용
                .order(order)       // 조회된 실제 엔티티 사용
                .seller(seller)     // 조회된 실제 엔티티 사용
                .rating(requestDTO.getRating())
                .content(requestDTO.getContent())
                .build();

        // 4. 저장
        SellerReview savedReview = reviewRepository.save(review);

        // 5. 주문의 리뷰 작성 여부 플래그 업데이트
        // (단일 리뷰만 허용하는 경우에 필요)
        order.setSellerReviewWritten(true);
        orderRepository.save(order); // 변경된 주문 상태 저장

        // 6. 이미지 저장
        List<String> imageUrls = saveImages(savedReview, requestDTO.getImageUrls());

        // 7. DTO로 변환
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

        // 주문의 리뷰 작성 여부 플래그 되돌리기 (리뷰가 삭제될 때만 해당 주문의 플래그를 false로 설정)
        // 해당 주문의 다른 리뷰가 있는지 확인하는 로직이 필요할 수 있으나,
        // 현재는 단일 주문-단일 리뷰 구조이므로 단순히 false로 설정
        // **주의: 만약 한 주문에 여러 리뷰가 가능하다면, 해당 주문에 남은 리뷰가 없을 때만 false로 변경하는 로직이 필요합니다.**
        Order order = review.getOrder();
        order.setSellerReviewWritten(false); // 리뷰 삭제 시 플래그를 false로 되돌림
        orderRepository.save(order); // 변경된 주문 상태 저장


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