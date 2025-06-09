package com.realive.event.review;

import com.realive.domain.review.SellerReview;
import com.realive.domain.review.SellerReviewImage;
import com.realive.repository.review.crud.SellerReviewImageRepository;

import com.realive.service.review.upload.ReviewImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList; // ArrayList 임포트

@Component
@RequiredArgsConstructor
@Log4j2
public class ReviewImageUploadHandler {

    private final ReviewImageUploader reviewImageUploader;
    private final SellerReviewImageRepository sellerReviewImageRepository;

    @Async("applicationEventTaskExecutor") // ⭐ 빈 이름 지정 ⭐
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewImageUpload(ReviewImageUploadEvent event) {

        SellerReview sellerReview = event.getSellerReview();
        List<String> tempImageUrls = event.getTempImageUrls();
        List<String> successfullyConfirmedUrls = new ArrayList<>(); // 성공적으로 확정된 URL들을 추적

        log.info("ReviewImageUploadHandler - 리뷰 이미지 확정 및 DB 저장 시작 (reviewId: {})", sellerReview.getId());

        try {
            if (tempImageUrls != null && !tempImageUrls.isEmpty()) {
                for (String tempUrl : tempImageUrls) {
                    if (tempUrl == null || tempUrl.isBlank()) {
                        log.warn("ReviewImageUploadHandler - 유효하지 않은 임시 이미지 URL 건너뛰기: {}", tempUrl);
                        continue;
                    }

                    try {
                        // 1. 임시 파일을 최종 위치로 이동하고 최종 URL을 얻음
                        String confirmedImageUrl = reviewImageUploader.confirmImage(tempUrl.trim(), sellerReview.getOrder().getId());
                        successfullyConfirmedUrls.add(confirmedImageUrl); // 성공한 URL만 추가

                        // 2. DB에 이미지 정보 저장
                        SellerReviewImage reviewImage = SellerReviewImage.builder()
                                .review(sellerReview)
                                .imageUrl(confirmedImageUrl)
                                .thumbnail(tempImageUrls.indexOf(tempUrl) == 0) // 첫 번째 이미지를 썸네일로
                                .build();
                        sellerReviewImageRepository.save(reviewImage);
                        log.debug("ReviewImageUploadHandler - 이미지 URL DB 저장 완료: reviewId={}, finalUrl={}", sellerReview.getId(), confirmedImageUrl);

                    } catch (IOException e) {
                        log.error("ReviewImageUploadHandler - 이미지 파일 이동 중 오류 발생: reviewId={}, tempUrl={}, error={}", sellerReview.getId(), tempUrl, e.getMessage(), e);
                        // 파일 이동 실패 시, 이 URL은 DB에 저장되지 않음. 다음 이미지 처리 계속.
                    } catch (IllegalArgumentException e) {
                        log.error("ReviewImageUploadHandler - 유효하지 않은 임시 이미지 URL: reviewId={}, tempUrl={}, error={}", sellerReview.getId(), tempUrl, e.getMessage());
                        // 유효하지 않은 URL 건너뛰기.
                    }
                }
            }
            log.info("ReviewImageUploadHandler - 리뷰 이미지 확정 및 DB 저장 처리 완료 (reviewId: {})", sellerReview.getId());

        } catch (Exception e) {
            log.error("ReviewImageUploadHandler - 리뷰 이미지 처리 중 예상치 못한 오류 발생: reviewId={}, error={}", sellerReview.getId(), e.getMessage(), e);

            throw new RuntimeException("리뷰 이미지 처리 중 오류가 발생했습니다.", e);
        }
    }
}