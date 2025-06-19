package com.realive.service.review.view; // ReviewQueryService 대신 기존 패키지명으로 변경

import com.realive.dto.review.MyReviewResponseDTO;
import com.realive.dto.review.ReviewListResponseDTO; // ReviewListResponseDTO 추가
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.repository.review.view.ReviewViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2; // Log4j2 추가
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2 // 로깅을 위한 어노테이션 추가
@Transactional(readOnly = true)
public class ReviewViewServiceImpl implements ReviewViewService { // ReviewQueryService 대신 ReviewViewServiceImpl 사용

    private final ReviewViewRepository reviewViewRepository;

    // 특정 리뷰 상세 정보 조회
    @Override // 인터페이스 구현 명시
    public ReviewResponseDTO getReviewDetail(Long id) { // getReviewDetail로 메서드명 변경
        log.info("리뷰 상세 정보 ID {}를 조회합니다.", id);
        if (id == null || id <= 0) {
            log.error("유효하지 않은 리뷰 ID: {}", id);
            throw new IllegalArgumentException("Invalid review ID");
        }

        Optional<ReviewResponseDTO> reviewDtoOpt = reviewViewRepository.findReviewDetailById(id);

        reviewDtoOpt.ifPresent(reviewDto -> {
            // 이미지 URL 조회 및 설정
            List<String> imageUrls = reviewViewRepository.findImageUrlsByReviewIds(List.of(reviewDto.getReviewId()))
                    .stream()
                    .map(tuple -> (String) tuple[1])
                    .collect(Collectors.toList());
            reviewDto.setImageUrls(imageUrls);

            // productName 조회 및 설정
            if (reviewDto.getOrderId() != null) {
                // 단일 주문 ID에 대한 상품 이름 조회 (List를 반환하지만 여기서는 맵의 첫 번째 값을 사용)
                List<Object[]> productNames = reviewViewRepository.findProductNamesByOrderIds(List.of(reviewDto.getOrderId()));
                productNames.stream().findFirst().ifPresent(tuple -> {
                    reviewDto.setProductName((String) tuple[1]); // tuple[1]이 productName
                });
            }
        });

        ReviewResponseDTO review = reviewDtoOpt.orElseThrow(() -> {
            log.error("ID {}를 가진 리뷰를 찾을 수 없습니다.", id);
            return new IllegalArgumentException("Review not found with id: " + id);
        });

        log.info("ID {}에 대한 리뷰 상세 정보를 조회했습니다.", id);
        return review;
    }

    // 판매자에 대한 리뷰 목록을 페이지네이션으로 조회
    @Override // 인터페이스 구현 명시
    public ReviewListResponseDTO getReviewList(Long sellerId, Pageable pageable) { // getReviewList로 메서드명 변경
        log.info("판매자 ID {}에 대한 리뷰 목록을 조회합니다. 페이지 번호: {}", sellerId, pageable.getPageNumber());
        if (sellerId == null || sellerId <= 0) {
            log.error("유효하지 않은 판매자 ID: {}", sellerId);
            throw new IllegalArgumentException("유효하지않은 판매자 ID입니다.");
        }

        Page<ReviewResponseDTO> reviewsPage = reviewViewRepository.findSellerReviewsBySellerId(sellerId, pageable);

        List<Long> reviewIds = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::getReviewId)
                .collect(Collectors.toList());

        List<Long> orderIds = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::getOrderId)
                .collect(Collectors.toList());

        // 이미지 URL 및 상품 이름 조회
        Map<Long, List<String>> reviewImageUrlsMap = reviewViewRepository.findImageUrlsByReviewIds(reviewIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> (Long) tuple[0],
                        Collectors.mapping(tuple -> (String) tuple[1], Collectors.toList())
                ));

        Map<Long, String> productNamesMap = reviewViewRepository.findProductNamesByOrderIds(orderIds)
                .stream()
                .collect(Collectors.toMap(
                        tuple -> (Long) tuple[0], // orderId
                        tuple -> (String) tuple[1] // productName
                ));

        // 각 리뷰 DTO에 이미지 URL 및 상품 이름 설정
        reviewsPage.getContent().forEach(reviewDto -> {
            reviewDto.setImageUrls(reviewImageUrlsMap.getOrDefault(reviewDto.getReviewId(), List.of()));
            if (reviewDto.getOrderId() != null) {
                reviewDto.setProductName(productNamesMap.get(reviewDto.getOrderId()));
            }
        });

        log.info("판매자 ID {}에 대한 총 {}개의 리뷰를 조회했습니다.", sellerId, reviewsPage.getTotalElements());

        return ReviewListResponseDTO.builder()
                .reviews(reviewsPage.getContent())
                .totalCount(reviewsPage.getTotalElements())
                .page(reviewsPage.getNumber())
                .size(reviewsPage.getSize())
                .build();
    }

    // 고객이 작성한 리뷰 목록을 페이지네이션으로 조회
    @Override // 인터페이스 구현 명시
    public Page<MyReviewResponseDTO> getMyReviewList(Long customerId, Pageable pageable) {
        log.info("고객 ID {}에 대한 내 리뷰 목록을 조회합니다. 페이지 번호: {}", customerId, pageable.getPageNumber());
        if (customerId == null || customerId <= 0) {
            log.error("유효하지 않은 고객 ID: {}", customerId);
            throw new IllegalArgumentException("Invalid customer ID");
        }

        Page<MyReviewResponseDTO> myReviewsPage = reviewViewRepository.findMyReviewsByCustomerId(customerId, pageable);

        List<Long> reviewIds = myReviewsPage.getContent().stream()
                .map(MyReviewResponseDTO::getReviewId)
                .collect(Collectors.toList());

        List<Long> orderIds = myReviewsPage.getContent().stream()
                .map(MyReviewResponseDTO::getOrderId)
                .collect(Collectors.toList());

        // 이미지 URL 및 상품 이름 조회
        Map<Long, List<String>> reviewImageUrlsMap = reviewViewRepository.findImageUrlsByReviewIds(reviewIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> (Long) tuple[0],
                        Collectors.mapping(tuple -> (String) tuple[1], Collectors.toList())
                ));

        Map<Long, String> productNamesMap = reviewViewRepository.findProductNamesByOrderIds(orderIds)
                .stream()
                .collect(Collectors.toMap(
                        tuple -> (Long) tuple[0], // orderId
                        tuple -> (String) tuple[1] // productName
                ));

        // 각 리뷰 DTO에 이미지 URL 및 상품 이름 설정
        myReviewsPage.getContent().forEach(reviewDto -> {
            reviewDto.setImageUrls(reviewImageUrlsMap.getOrDefault(reviewDto.getReviewId(), List.of()));
            if (reviewDto.getOrderId() != null) {
                reviewDto.setProductName(productNamesMap.get(reviewDto.getOrderId()));
            }
        });

        log.info("고객 ID {}에 대한 총 {}개의 리뷰를 조회했습니다.", customerId, myReviewsPage.getTotalElements());
        return myReviewsPage;
    }
}