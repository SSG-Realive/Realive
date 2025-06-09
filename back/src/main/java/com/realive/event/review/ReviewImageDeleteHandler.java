package com.realive.event.review;

import com.realive.service.review.upload.ReviewImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException; // IOException import 추가
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class ReviewImageDeleteHandler {

    private final ReviewImageUploader reviewImageUploader;

    @Async("applicationEventTaskExecutor")
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

                try {
                    if (imageUrl.contains("/temp/")) {
                        reviewImageUploader.deleteTempImage(imageUrl);
                    } else {
                        reviewImageUploader.deleteFinalImage(imageUrl);
                    }
                } catch (IOException e) {
                    log.error("ReviewImageDeleteHandler - 이미지 파일 삭제 중 오류 발생: URL={}, 에러={}", imageUrl, e.getMessage(), e);
                    // 특정 파일 삭제 실패 시에도 다른 파일 삭제는 계속 진행되도록 합니다.
                    // 필요에 따라 이 예외를 상위로 다시 던지거나, 트랜잭션을 롤백하는 등의 추가 처리를 고려할 수 있습니다.
                }
            }
        }
        log.info("ReviewImageDeleteHandler - 리뷰 이미지 파일 삭제 처리 완료");
    }
}