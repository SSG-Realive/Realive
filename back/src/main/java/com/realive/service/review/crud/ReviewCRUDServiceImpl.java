package com.realive.service.review.crud;

import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderItem;
import com.realive.domain.review.SellerReview;
import com.realive.domain.review.SellerReviewImage;
import com.realive.domain.seller.Seller;
import com.realive.dto.review.ReviewCreateRequestDTO;
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.dto.review.ReviewUpdateRequestDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderItemRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.review.crud.ReviewCRUDRepository;
import com.realive.repository.review.crud.SellerReviewImageRepository;
import com.realive.repository.seller.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReviewCRUDServiceImpl implements ReviewCRUDService {
    private final ReviewCRUDRepository reviewRepository;
    private final SellerReviewImageRepository imageRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewCreateRequestDTO requestDTO, Long customerId) {
        // 이미 해당 주문, 고객, 판매자 조합으로 리뷰가 있는지 확인
        reviewRepository.findByOrderIdAndCustomerIdAndSellerId(requestDTO.getOrderId(), customerId, requestDTO.getSellerId())
                .ifPresent(review -> {
                    log.warn("createReview - 이미 작성된 리뷰 시도: orderId={}, customerId={}, sellerId={}",
                            requestDTO.getOrderId(), customerId, requestDTO.getSellerId());
                    throw new IllegalStateException("이미 작성하신 리뷰입니다. 하나의 주문/판매자 조합에 하나의 리뷰만 작성 가능합니다.");
                });

        // 엔티티 조회 및 유효성 검사
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.error("createReview - 존재하지 않는 고객 ID: {}", customerId);
                    return new EntityNotFoundException("존재하지 않는 고객입니다.: " + customerId);
                });
        Order order = orderRepository.findById(requestDTO.getOrderId())
                .orElseThrow(() -> {
                    log.error("createReview - 존재하지 않는 주문 ID: {}", requestDTO.getOrderId());
                    return new EntityNotFoundException("존재하지 않는 주문입니다.: " + requestDTO.getOrderId());
                });
        // Seller는 DTO에서 받아오므로, 그대로 사용
        Seller seller = sellerRepository.findById(requestDTO.getSellerId())
                .orElseThrow(() -> {
                    log.error("createReview - 존재하지 않는 판매자 ID: {}", requestDTO.getSellerId());
                    return new EntityNotFoundException("존재하지 않는 판매자입니다.: " + requestDTO.getSellerId());
                });

        // SellerReview 엔티티 생성 및 저장
        SellerReview review = SellerReview.builder()
                .customer(customer)
                .order(order)
                .seller(seller)
                .rating(requestDTO.getRating())
                .content(requestDTO.getContent())
                .build();

        SellerReview savedReview = reviewRepository.save(review);
        log.info("createReview - 새 리뷰 저장 성공: reviewId={}", savedReview.getId());

        // 리뷰 이미지 처리: DTO의 이미지 URL을 바탕으로 SellerReviewImage 엔티티를 생성하고 저장합니다.
        List<String> savedImageUrls = List.of();
        if (requestDTO.getImageUrls() != null && !requestDTO.getImageUrls().isEmpty()) {
            List<SellerReviewImage> reviewImagesToSave = requestDTO.getImageUrls().stream()
                    .map(imageUrl -> {
                        boolean isThumbnail = requestDTO.getImageUrls().indexOf(imageUrl) == 0; // 첫 번째 이미지를 썸네일로 설정
                        return SellerReviewImage.builder()
                                .review(savedReview)
                                .imageUrl(imageUrl)
                                .thumbnail(isThumbnail)
                                .build();
                    })
                    .collect(Collectors.toList());
            imageRepository.saveAll(reviewImagesToSave);
            log.info("createReview - 리뷰 이미지 DB 저장 완료: reviewId={}, 이미지 수: {}", savedReview.getId(), reviewImagesToSave.size());

            savedImageUrls = imageRepository.findByReviewId(savedReview.getId())
                    .stream()
                    .map(SellerReviewImage::getImageUrl)
                    .collect(Collectors.toList());
        }

        String productName = getProductNameForReview(order, seller);

        return ReviewResponseDTO.builder()
                .reviewId(savedReview.getId())
                .orderId(savedReview.getOrder().getId())
                .customerId(savedReview.getCustomer().getId())
                .sellerId(savedReview.getSeller().getId())
                .productName(productName)
                .rating(savedReview.getRating())
                .content(savedReview.getContent())
                .imageUrls(savedImageUrls)
                .createdAt(savedReview.getCreatedAt())
                .isHidden(savedReview.isHidden())
                .build();
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateRequestDTO requestDTO, Long customerId) {
        SellerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("updateReview - 해당하는 리뷰를 찾을 수 없습니다.: {}", reviewId);
                    return new EntityNotFoundException("해당하는 리뷰를 찾을 수 없습니다. 리뷰 ID: " + reviewId);
                });

        if (!review.getCustomer().getId().equals(customerId)) {
            log.warn("updateReview - 리뷰 수정 권한 없음: reviewId={}, customerId={}, attemptingCustomerId={}",
                    reviewId, review.getCustomer().getId(), customerId);
            throw new SecurityException("리뷰를 수정할 수 있는 권한이 없습니다.");
        }

        imageRepository.deleteByReviewId(reviewId);
        log.info("updateReview - 기존 리뷰 이미지 DB에서 삭제 완료: reviewId={}", reviewId);

        review.setRating(requestDTO.getRating());
        review.setContent(requestDTO.getContent());
        SellerReview updatedReview = reviewRepository.save(review);
        log.info("updateReview - 리뷰 내용 업데이트 성공: reviewId={}", updatedReview.getId());

        List<String> finalImageUrls = List.of();
        if (requestDTO.getImageUrls() != null && !requestDTO.getImageUrls().isEmpty()) {
            List<SellerReviewImage> reviewImagesToSave = requestDTO.getImageUrls().stream()
                    .map(imageUrl -> {
                        boolean isThumbnail = requestDTO.getImageUrls().indexOf(imageUrl) == 0;
                        return SellerReviewImage.builder()
                                .review(updatedReview)
                                .imageUrl(imageUrl)
                                .thumbnail(isThumbnail)
                                .build();
                    })
                    .collect(Collectors.toList());
            imageRepository.saveAll(reviewImagesToSave);
            log.info("updateReview - 새 리뷰 이미지 DB 저장 완료: reviewId={}, 이미지 수: {}", updatedReview.getId(), reviewImagesToSave.size());

            finalImageUrls = imageRepository.findByReviewId(updatedReview.getId())
                    .stream()
                    .map(SellerReviewImage::getImageUrl)
                    .collect(Collectors.toList());
        }

        String productName = getProductNameForReview(updatedReview.getOrder(), updatedReview.getSeller());

        return ReviewResponseDTO.builder()
                .reviewId(updatedReview.getId())
                .orderId(updatedReview.getOrder().getId())
                .customerId(updatedReview.getCustomer().getId())
                .sellerId(updatedReview.getSeller().getId())
                .productName(productName)
                .rating(updatedReview.getRating())
                .content(updatedReview.getContent())
                .imageUrls(finalImageUrls)
                .createdAt(updatedReview.getCreatedAt())
                .isHidden(updatedReview.isHidden())
                .build();
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long customerId) {
        SellerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("deleteReview - 해당하는 리뷰를 찾을 수 없습니다.: {}", reviewId);
                    return new EntityNotFoundException("해당하는 리뷰를 찾을 수 없습니다. 리뷰 ID: " + reviewId);
                });

        if (!review.getCustomer().getId().equals(customerId)) {
            log.warn("deleteReview - 리뷰 삭제 권한 없음: reviewId={}, customerId={}, attemptingCustomerId={}",
                    reviewId, review.getCustomer().getId(), customerId);
            throw new SecurityException("리뷰를 삭제하실 수 있는 권한이 없습니다.");
        }

        imageRepository.deleteByReviewId(reviewId);
        log.info("deleteReview - 리뷰 이미지 DB에서 삭제 완료: reviewId={}", reviewId);

        reviewRepository.delete(review);
        log.info("deleteReview - 리뷰 삭제 성공: reviewId={}", reviewId);
    }

    @Override
    public boolean checkReviewExistence(Long orderId, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 고객입니다.: " + customerId));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        if (orderItems.isEmpty()) {
            log.warn("checkReviewExistence - 주문 ID {}에 해당하는 OrderItem이 없습니다.", orderId);
            return false;
        }

        // 주문의 첫 번째 아이템의 판매자 ID를 사용하여 해당 주문/고객/판매자 조합의 리뷰 존재 여부를 확인합니다.
        // 이는 createReview의 findByOrder_IdAndCustomer_IdAndSeller_Id와 일관성을 유지하기 위함입니다.
        // 한 주문에 여러 판매자의 상품이 있을 경우, 이 로직은 첫 번째 상품의 판매자에 대한 리뷰만 확인합니다.
        // 모든 판매자에 대한 리뷰 존재 여부를 확인하려면, orderItems를 순회하며 각 판매자에 대해 체크해야 합니다.
        // 현재 프론트엔드 로직은 단일 판매자에 대한 리뷰를 가정하고 있습니다.
        Long sellerIdOfFirstItemInOrder = orderItems.get(0).getProduct().getSeller().getId();

        return reviewRepository.findByOrderIdAndCustomerIdAndSellerId(orderId, customerId, sellerIdOfFirstItemInOrder).isPresent();
    }

    private String getProductNameForReview(Order order, Seller seller) {
        return orderItemRepository.findByOrderId(order.getId()).stream()
                .filter(orderItem -> orderItem.getProduct() != null && orderItem.getProduct().getSeller() != null &&
                        orderItem.getProduct().getSeller().getId().equals(seller.getId()))
                .map(orderItem -> orderItem.getProduct().getName())
                .findFirst()
                .orElse(null);
    }
}