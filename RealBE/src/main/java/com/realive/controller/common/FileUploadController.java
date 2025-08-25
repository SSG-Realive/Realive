package com.realive.controller.common;

import com.realive.service.common.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

//[AWS] 이미지를 S3에 업로드 컨트롤러

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FileUploadController {

    private final S3Uploader s3Uploader; //AWS S3에 파일 업로드 서비스

    //[Customer] 리뷰 이미지 저장
    @PostMapping("/customer/reviews/images/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = s3Uploader.upload(file, "uploads"); // uploads 폴더에 저장
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }
    
}

