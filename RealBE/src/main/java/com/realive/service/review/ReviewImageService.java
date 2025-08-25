//package com.realive.service.review;
//
//import com.realive.domain.review.SellerReview;
//import com.realive.domain.review.SellerReviewImage;
//import com.realive.repository.review.ReviewImageRepository;
//import com.realive.repository.review.SellerReviewRepository;
//import com.realive.repository.review.crud.ReviewCRUDRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ReviewImageService {
//
//    private final ReviewImageRepository reviewImageRepository;
//    private final ReviewCRUDRepository reviewCRUDRepository;
//
//
//    // 이미지 저장소 서비스 (S3 등)
//    private final ImageStorageService imageStorageService;
//
//    /**
//     * 리뷰 이미지 업로드
//     */
//    @Transactional
//    public List<String> uploadImages(Long reviewId, List<MultipartFile> files) {
//        SellerReview review = reviewCRUDRepository.findById(reviewId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다. ID=" + reviewId));
//
//        List<SellerReviewImage> savedImages = files.stream()
//                .map(file -> {
//                    String imageUrl = imageStorageService.uploadFile(file, generateFileName(file));
//                    return SellerReviewImage.builder()
//                            .review(review)
//                            .imageUrl(imageUrl)          // url 컬럼에 값 세팅
//                            // image_url 컬럼에 값 세팅
//                            .thumbnail(false)
//                            .build();
//                })
//
//                .map(reviewImageRepository::save)
//                .collect(Collectors.toList());
//
//        return savedImages.stream()
//                .map(SellerReviewImage::getImageUrl)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * 리뷰 ID로 이미지 삭제
//     */
//    @Transactional
//    public void deleteImagesByReviewId(Long reviewId) {
//        List<SellerReviewImage> images = reviewImageRepository.findByReviewId(reviewId);
//        for (SellerReviewImage image : images) {
//            imageStorageService.deleteFile(image.getImageUrl());
//        }
//        reviewImageRepository.deleteByReviewId(reviewId);
//    }
//
//
//    //파일 이름 생성
//
//    private String generateFileName(MultipartFile file) {
//        String ext = getFileExtension(file.getOriginalFilename());
//        return UUID.randomUUID() + "." + ext;
//    }
//
//
//    private String getFileExtension(String originalName) {
//        if (originalName == null || !originalName.contains(".")) {
//            throw new IllegalArgumentException("유효하지 않은 파일명입니다.");
//        }
//        return originalName.substring(originalName.lastIndexOf('.') + 1);
//    }
//}
