//package com.realive.service.review.crud;
//
//import com.realive.domain.customer.Customer;
//import com.realive.domain.order.Order;
//import com.realive.domain.seller.SellerReview;
//import com.realive.domain.review.SellerReviewImage;
//import com.realive.domain.seller.Seller;
//import com.realive.dto.review.ReviewCreateRequestDTO;
//import com.realive.dto.review.ReviewResponseDTO;
//import com.realive.dto.review.ReviewUpdateRequestDTO;
//import com.realive.repository.customer.CustomerRepository;
//import com.realive.repository.order.OrderRepository;
//import com.realive.repository.review.crud.ReviewCRUDRepository;
//import com.realive.repository.review.crud.SellerReviewImageRepository;
//import com.realive.repository.seller.SellerRepository;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class ReviewCRUDServiceImpl implements ReviewCRUDService {
//    private final ReviewCRUDRepository reviewRepository;
//    private final SellerReviewImageRepository imageRepository;
//    private final CustomerRepository customerRepository;
//    private final OrderRepository orderRepository;
//    private final SellerRepository sellerRepository;
//
//    @Override
//    @Transactional
//    public ReviewResponseDTO createReview(ReviewCreateRequestDTO requestDTO, Long customerId) {
//        // 1. 중복 리뷰 확인 (orderId, customerId, sellerId 조합으로 확인)
//        reviewRepository.findByOrderIdAndCustomerIdAndSellerId(requestDTO.getOrderId(), customerId, requestDTO.getSellerId())
//                .ifPresent(review -> {
//                    throw new IllegalStateException("A review for this order and seller by this customer already exists.");
//                });
//
//        // 2. Customer, Order, Seller 엔티티 조회 (실제 존재하는지 확인)
//        Customer customer = customerRepository.findById(customerId)
//                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + customerId));
//        Order order = orderRepository.findById(requestDTO.getOrderId())
//                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다.: " + requestDTO.getOrderId()));
//        Seller seller = sellerRepository.findById(requestDTO.getSellerId())
//                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 판매자입니다.: " + requestDTO.getSellerId()));
//
//        // 3. SellerReview 엔티티 생성
//        SellerReview review = SellerReview.builder()
//                .customer(customer)
//                .order(order)
//                .seller(seller)
//                .rating(requestDTO.getRating())
//                .content(requestDTO.getContent())
//                .build();
//
//        // 4. 저장
//        SellerReview savedReview = reviewRepository.save(review);
//
//        // 5. 이미지 저장 (createdAt 수동 설정 부분 제거)
//        List<String> imageUrls = saveImages(savedReview, requestDTO.getImageUrls());
//
//        // 6. DTO로 변환하여 반환
//        return ReviewResponseDTO.builder()
//                .reviewId(savedReview.getId())
//                .orderId(savedReview.getOrder().getId())
//                .customerId(savedReview.getCustomer().getId())
//                .sellerId(savedReview.getSeller().getId())
//                .productName(null) // CRUD 서비스에서는 productName을 처리하지 않습니다.
//                .rating(savedReview.getRating())
//                .content(savedReview.getContent())
//                .imageUrls(imageUrls)
//                .createdAt(savedReview.getCreatedAt())
//                .isHidden(savedReview.isHidden())
//                .build();
//    }
//
//    @Override
//    @Transactional
//    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateRequestDTO requestDTO, Long customerId) {
//        // 리뷰 조회
//        SellerReview review = reviewRepository.findById(reviewId)
//                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. " + reviewId));
//
//        // 권한 확인
//        if (!review.getCustomer().getId().equals(customerId)) {
//            throw new SecurityException("리뷰를 수정할 수 있는 권한이 없습니다.");
//        }
//
//        // 리뷰 내용 업데이트
//        review.setRating(requestDTO.getRating());
//        review.setContent(requestDTO.getContent());
//
//        // 기존 이미지 삭제
//        imageRepository.deleteByReviewId(reviewId);
//
//        // 새 이미지 저장
//        List<String> imageUrls = saveImages(review, requestDTO.getImageUrls());
//
//        SellerReview updatedReview = reviewRepository.save(review); // 변경된 리뷰 저장
//
//        // DTO로 변환
//        return ReviewResponseDTO.builder()
//                .reviewId(updatedReview.getId())
//                .orderId(updatedReview.getOrder().getId())
//                .customerId(updatedReview.getCustomer().getId())
//                .sellerId(updatedReview.getSeller().getId())
//                .productName(null) // CRUD 서비스에서는 productName을 처리하지 않습니다.
//                .rating(updatedReview.getRating())
//                .content(updatedReview.getContent())
//                .imageUrls(imageUrls)
//                .createdAt(updatedReview.getCreatedAt())
//                .isHidden(updatedReview.isHidden())
//                .build();
//    }
//
//    @Override
//    @Transactional
//    public void deleteReview(Long reviewId, Long customerId) {
//        // 리뷰 조회
//        SellerReview review = reviewRepository.findById(reviewId)
//                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. : " + reviewId));
//
//        // 권한 확인
//        if (!review.getCustomer().getId().equals(customerId)) {
//            throw new SecurityException("리뷰를 삭제하실 수 있는 권한이 없습니다.");
//        }
//
//        // 관련 이미지 삭제 (먼저 삭제하여 외래키 제약조건 위반 방지)
//        imageRepository.deleteByReviewId(reviewId);
//
//        // 리뷰 삭제
//        reviewRepository.delete(review);
//    }
//
//    // 이미지 저장을 위한 헬퍼 메서드
//    private List<String> saveImages(SellerReview review, List<String> imageUrls) {
//        List<String> savedImageUrls = new ArrayList<>();
//        if (imageUrls != null && !imageUrls.isEmpty()) {
//            List<SellerReviewImage> reviewImages = imageUrls.stream()
//                    .filter(url -> url != null && !url.isBlank())
//                    .map(url -> SellerReviewImage.builder()
//                            .review(review)
//                            .imageUrl(url)
//                            .thumbnail(imageUrls.indexOf(url) == 0)
//                            .build())
//                    .collect(Collectors.toList());
//            imageRepository.saveAll(reviewImages);
//            savedImageUrls = reviewImages.stream()
//                    .map(SellerReviewImage::getImageUrl)
//                    .collect(Collectors.toList());
//        }
//        return savedImageUrls;
//    }
//}