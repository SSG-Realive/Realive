package com.realive.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value; // @Value 어노테이션 사용을 위해 추가

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component // Spring Bean으로 등록
public class LocalDiskImageUploader implements ImageUploader {

    // 이미지를 저장할 로컬 디렉토리 경로 (application.properties 등에서 설정)
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String uploadImage(MultipartFile file) {
        return uploadImage(file, null);
    }

    @Override
    public String uploadImage(MultipartFile file, String prefix) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            // 업로드 디렉토리 생성 (없으면)
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 파일명에 접두사 적용
            String fileName = (prefix != null && !prefix.isEmpty() ? prefix : "") + uuid + fileExtension;
            Path filePath = uploadPath.resolve(fileName);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath);

            // 로컬 파일의 경우 URL은 서비스할 서버의 경로를 가정 (예: /images/{fileName})
            // 실제 운영 환경에서는 Nginx 등을 통해 정적 파일 서비스하거나, 클라우드 스토리지 URL 사용
            return "/images/" + fileName; // 예시 URL 반환
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to local disk: " + file.getOriginalFilename(), e);
        }
    }
}