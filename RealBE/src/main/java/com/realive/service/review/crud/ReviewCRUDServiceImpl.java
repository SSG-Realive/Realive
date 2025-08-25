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
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//[Customer] 고객 리뷰 CRUD 서비스

@Log4j2
@Service
@RequiredArgsConstructor
public class ReviewCRUDServiceImpl implements ReviewCRUDService {

    private final ReviewCRUDRepository reviewRepository;
    private final SellerReviewImageRepository imageRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final SellerRepository sellerRepository;

    //리뷰 생성
    /* order내의 판매자별 리뷰입니다.*/
    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewCreateRequestDTO requestDTO, Long customerId) {

        // 1. 중복 리뷰 확인 (order + customer + seller 조합)
        reviewRepository.findByOrderIdAndCustomerIdAndSellerId(
                        requestDTO.getOrderId(), customerId, requestDTO.getSellerId())
                .ifPresent(review -> {
                    throw new IllegalStateException("이미 리뷰가 존재합니다.");
                });

        // 2. 엔티티 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("고객을 찾을 수 없습니다. ID=" + customerId));

        Order order = orderRepository.findById(requestDTO.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다. ID=" + requestDTO.getOrderId()));

        Seller seller = sellerRepository.findById(requestDTO.getSellerId())
                .orElseThrow(() -> new EntityNotFoundException("판매자를 찾을 수 없습니다. ID=" + requestDTO.getSellerId()));

        // 3. 리뷰 생성
        SellerReview review = SellerReview.builder()
                .customer(customer)
                .order(order)
                .seller(seller)
                .rating(requestDTO.getRating())
                .content(requestDTO.getContent())
                .isHidden(false)
                .build();

        SellerReview savedReview = reviewRepository.save(review);

        // 4. 이미지 저장
        List<String> savedImageUrls = saveImages(savedReview, requestDTO.getImageUrls());

        // 5. DTO 반환
        return ReviewResponseDTO.builder()
                .reviewId(savedReview.getId())
                .orderId(order.getId())
                .customerId(customerId)
                .sellerId(seller.getId())
                .productName(null) // 필요시 서비스에서 order_items로 상품명 세팅 가능
                .rating(savedReview.getRating())
                .content(savedReview.getContent())
                .imageUrls(savedImageUrls)
                .createdAt(savedReview.getCreatedAt())
                .isHidden(savedReview.isHidden())
                .build();
    }

    //리뷰 수정
    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateRequestDTO requestDTO, Long customerId) {

        log.info("updateReview 호출 - reviewId: {}, customerId: {}", reviewId, customerId);
        log.info("updateReview - 전달받은 imageUrls: {}", requestDTO.getImageUrls());

        SellerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. ID=" + reviewId));

        if (!review.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("리뷰 수정 권한이 없습니다.");
        }

        review.setRating(requestDTO.getRating());
        review.setContent(requestDTO.getContent());

        // 기존 이미지 삭제
        imageRepository.deleteByReviewId(reviewId);

        // 새 이미지 저장
        saveImages(review, requestDTO.getImageUrls());

        SellerReview updatedReview = reviewRepository.save(review);

        List<String> imageUrls = imageRepository.findByReviewId(reviewId).stream()
                .map(SellerReviewImage::getImageUrl)
                .collect(Collectors.toList());

        return ReviewResponseDTO.builder()
                .reviewId(updatedReview.getId())
                .orderId(updatedReview.getOrder().getId())
                .customerId(customerId)
                .sellerId(updatedReview.getSeller().getId())
                .productName(null)
                .rating(updatedReview.getRating())
                .content(updatedReview.getContent())
                .imageUrls(imageUrls)
                .createdAt(updatedReview.getCreatedAt())
                .isHidden(updatedReview.isHidden())
                .build();
    }


    //리뷰 삭제
    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long customerId) {
        SellerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. ID=" + reviewId));

        if (!review.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("리뷰 삭제 권한이 없습니다.");
        }

        imageRepository.deleteByReviewId(reviewId);
        reviewRepository.delete(review);
    }

    /* 이미지를 저장하는 메서드 입니다.*/
    private List<String> saveImages(SellerReview review, List<String> imageUrls) {

        //이미지가 없을 경우:
        if (imageUrls == null) return List.of();

        List<SellerReviewImage> images = imageUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(url -> SellerReviewImage.builder()
                        .review(review) //review에 연결
                        .imageUrl(url)
                        .thumbnail(imageUrls.indexOf(url) == 0) //첫 번째 이미지를 thunbnail로 지정
                        .build())
                .collect(Collectors.toList());

        imageRepository.saveAll(images);

        //저장된 이미지들의 URL만 반환
        return images.stream()
                .map(SellerReviewImage::getImageUrl)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkReviewExistence(Long orderId, Long customerId, Long sellerId) {
        return reviewRepository.findByOrderIdAndCustomerIdAndSellerId(orderId, customerId, sellerId).isPresent();
    }

}
