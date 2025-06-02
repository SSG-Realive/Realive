package com.realive.event.review;

import com.realive.service.review.upload.ReviewImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class ReviewImageDeleteHandler {

    private final ReviewImageUploader reviewImageUploader;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewImageDelete(ReviewImageDeleteEvent event) {
        List<String> imageUrlsToDelete = event.getImageUrlsToDelete();

        log.info("ReviewImageDeleteHandler - 리뷰 이미지 파일 삭제 시작 (총 {}개)", imageUrlsToDelete.size());

        if (imageUrlsToDelete != null && !imageUrlsToDelete.isEmpty()) {
            for (String imageUrl : imageUrlsToDelete) {
                if (imageUrl == null || imageUrl.isBlank()) {
                    log.warn("ReviewImageDeleteHandler - 유효하지 않은 삭제 이미지 URL 건너뛰기: {}", imageUrl);
                    continue;
                }
                reviewImageUploader.deleteFinalImage(imageUrl); // ReviewImageUploader의 삭제 메서드 호출
            }
        }
        log.info("ReviewImageDeleteHandler - 리뷰 이미지 파일 삭제 처리 완료");
    }
}