//package com.realive.controller.customer;
//
//import com.realive.service.review.ReviewImageService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Collections;
//import java.util.List;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/customer/reviews/images")
//@RequiredArgsConstructor
//public class ReviewImageController {
//
//    private final S3Uploader s3Uploader;
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadReviewImage(@RequestPart("file") MultipartFile file) {
//        try {
//            String url = s3Uploader.uploadFile(file, "reviews");
//            return ResponseEntity.ok(url);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("업로드 실패: " + e.getMessage());
//        }
//    }
//
//
//    private final ReviewImageService reviewImageService;
//
//    /**
//     * 리뷰 이미지 업로드
//     */
//    @PostMapping("/upload")
//    public ResponseEntity<List<String>> uploadImages(
//            @RequestParam Long reviewId,
//            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
//
//        if (files == null || files.isEmpty()) {
//            // 파일 없으면 빈 리스트 반환하거나 적절한 응답
//            log.warn("업로드할 파일이 없습니다. reviewId={}", reviewId);
//            return ResponseEntity.badRequest().body(Collections.emptyList());
//        }
//
//        List<String> uploadedUrls = reviewImageService.uploadImages(reviewId, files);
//        return ResponseEntity.ok(uploadedUrls);
//    }
//
//
//    /**
//     * 리뷰 이미지 전체 삭제
//     * @param reviewId 리뷰 ID
//     * @return 삭제 완료 메시지
//     */
//    @DeleteMapping("/delete")
//    public ResponseEntity<String> deleteImages(@RequestParam Long reviewId) {
//        reviewImageService.deleteImagesByReviewId(reviewId);
//        return ResponseEntity.ok("리뷰 이미지가 삭제되었습니다.");
//    }
//}

