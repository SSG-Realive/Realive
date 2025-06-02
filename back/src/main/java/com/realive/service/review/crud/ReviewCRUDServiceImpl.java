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
import com.realive.event.review.ReviewImageUploadEventPublisher; // 새 이벤트 발행자 임포트
import com.realive.event.review.ReviewImageDeleteEventPublisher; // 새 이벤트 발행자 임포트
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
    private final SellerRepository sellerRepository;
    private final ReviewImageUploadEventPublisher reviewImageUploadEventPublisher; // 주입
    private final ReviewImageDeleteEventPublisher reviewImageDeleteEventPublisher; // 주입

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewCreateRequestDTO requestDTO, Long customerId) {
        reviewRepository.findByOrderIdAndCustomerIdAndSellerId(requestDTO.getOrderId(), customerId, requestDTO.getSellerId())
                .ifPresent(review -> {
                    log.warn("createReview - 이미 작성된 리뷰 시도: orderId={}, customerId={}, sellerId={}",
                            requestDTO.getOrderId(), customerId, requestDTO.getSellerId());
                    throw new IllegalStateException("이미 작성하신 리뷰입니다. 하나의 주문/판매자 조합에 하나의 리뷰만 작성 가능합니다.");
                });

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
        Seller seller = sellerRepository.findById(requestDTO.getSellerId())
                .orElseThrow(() -> {
                    log.error("createReview - 존재하지 않는 판매자 ID: {}", requestDTO.getSellerId());
                    return new EntityNotFoundException("존재하지 않는 판매자입니다.: " + requestDTO.getSellerId());
                });

        SellerReview review = SellerReview.builder()
                .customer(customer)
                .order(order)
                .seller(seller)
                .rating(requestDTO.getRating())
                .content(requestDTO.getContent())
                .build();

        SellerReview savedReview = reviewRepository.save(review);
        log.info("createReview - 새 리뷰 저장 성공: reviewId={}", savedReview.getId());

        // 이미지를 DB에 저장하는 대신, 이미지 확정 이벤트를 발행합니다.
        // 클라이언트에서 먼저 업로드하여 받은 '임시' URL들을 이벤트에 실어 보냅니다.
        if (requestDTO.getImageUrls() != null && !requestDTO.getImageUrls().isEmpty()) {
            reviewImageUploadEventPublisher.publish(savedReview, requestDTO.getImageUrls());
            log.info("createReview - 리뷰 이미지 확정 이벤트 발행: reviewId={}, 이미지 수: {}", savedReview.getId(), requestDTO.getImageUrls().size());
        }

        // DTO 반환 시, 이미지는 이벤트 핸들러에 의해 비동기적으로 저장되므로
        // 이 시점에서는 아직 DB에 저장되지 않았을 수 있습니다.
        // 따라서, 초기 DTO에서는 이미지 URL을 빈 리스트로 반환하거나
        // `requestDTO.getImageUrls()`를 임시로 반환하고,
        // 클라이언트가 이후에 이미지가 완전히 처리된 후 다시 조회하도록 하는 것이 일반적입니다.
        // 여기서는 임시로 requestDTO.getImageUrls()를 반환합니다.
        // 또는 트랜잭션 커밋 후 DB에서 조회하는 방법도 가능하지만, 이는 동기적으로 동작해야 합니다.
        // 가장 안전한 방법은 커밋 후 imageRepository.findBySellerReviewId(savedReview.getId())를 호출하는 것입니다.
        // 현재는 requestDTO.getImageUrls()를 반환하고, 실제 이미지 URL은 핸들러가 처리 후 DB에 기록합니다.
        // 그리고 클라이언트가 나중에 API 조회로 가져가도록 합니다.
        return ReviewResponseDTO.builder()
                .reviewId(savedReview.getId())
                .orderId(savedReview.getOrder().getId())
                .customerId(savedReview.getCustomer().getId())
                .sellerId(savedReview.getSeller().getId())
                .productName(null)
                .rating(savedReview.getRating())
                .content(savedReview.getContent())
                .imageUrls(requestDTO.getImageUrls() != null ? requestDTO.getImageUrls() : List.of()) // 초기에는 요청받은 URL 반환
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

        // 기존 이미지 URL들을 DB에서 미리 가져옵니다. (파일 삭제 이벤트를 위해)
        List<String> oldImageUrls = imageRepository.findBySellerReviewId(reviewId)
                .stream()
                .map(SellerReviewImage::getImageUrl)
                .collect(Collectors.toList());

        // 기존 이미지들을 DB에서 삭제합니다. (파일 시스템 삭제는 이벤트 핸들러가 처리)
        imageRepository.deleteByReviewId(reviewId);
        log.info("updateReview - 기존 리뷰 이미지 DB에서 삭제 완료: reviewId={}", reviewId);

        // 기존 이미지 파일 삭제 이벤트를 발행합니다.
        if (!oldImageUrls.isEmpty()) {
            reviewImageDeleteEventPublisher.publish(oldImageUrls);
            log.info("updateReview - 기존 이미지 파일 삭제 이벤트 발행: reviewId={}, 이미지 수: {}", reviewId, oldImageUrls.size());
        }

        review.setRating(requestDTO.getRating());
        review.setContent(requestDTO.getContent());
        SellerReview updatedReview = reviewRepository.save(review);
        log.info("updateReview - 리뷰 내용 업데이트 성공: reviewId={}", updatedReview.getId());

        // 새 이미지가 있다면 확정 이벤트를 발행합니다.
        if (requestDTO.getImageUrls() != null && !requestDTO.getImageUrls().isEmpty()) {
            reviewImageUploadEventPublisher.publish(updatedReview, requestDTO.getImageUrls());
            log.info("updateReview - 새 리뷰 이미지 확정 이벤트 발행: reviewId={}, 이미지 수: {}", updatedReview.getId(), requestDTO.getImageUrls().size());
        }

        // DTO 반환 시, 업데이트된 이미지 목록을 DB에서 조회하여 반환하는 것이 가장 정확합니다.
        List<String> finalImageUrlsFromDb = imageRepository.findBySellerReviewId(updatedReview.getId())
                .stream()
                .map(SellerReviewImage::getImageUrl)
                .collect(Collectors.toList());

        return ReviewResponseDTO.builder()
                .reviewId(updatedReview.getId())
                .orderId(updatedReview.getOrder().getId())
                .customerId(updatedReview.getCustomer().getId())
                .sellerId(updatedReview.getSeller().getId())
                .productName(null)
                .rating(updatedReview.getRating())
                .content(updatedReview.getContent())
                .imageUrls(finalImageUrlsFromDb)
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

        // 삭제할 이미지 URL들을 DB에서 미리 가져옵니다.
        List<String> deletedImageUrls = imageRepository.findBySellerReviewId(reviewId)
                .stream()
                .map(SellerReviewImage::getImageUrl)
                .collect(Collectors.toList());

        // 관련 이미지 DB에서 삭제 (파일 시스템 삭제는 이벤트 핸들러가 처리)
        imageRepository.deleteByReviewId(reviewId);
        log.info("deleteReview - 리뷰 이미지 DB에서 삭제 완료: reviewId={}", reviewId);

        // 리뷰 삭제
        reviewRepository.delete(review);
        log.info("deleteReview - 리뷰 삭제 성공: reviewId={}", reviewId);

        // 이미지 파일 삭제 이벤트를 발행합니다.
        if (!deletedImageUrls.isEmpty()) {
            reviewImageDeleteEventPublisher.publish(deletedImageUrls);
            log.info("deleteReview - 이미지 파일 삭제 이벤트 발행: reviewId={}, 이미지 수: {}", reviewId, deletedImageUrls.size());
        }
    }
}