package com.realive.service.common;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

//[AWS] AWS S3에 파일 업로드 서비스

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}") //S3 버킷 이름
    private String bucket;

    public String upload(MultipartFile file, String dirName) throws IOException {
        //파라미터: (1)업로드할 파일 (2)S3 버킷 내 저장할 디렉토리
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        //실제 S3 버킷에 파일 스트림과 메타데이터를 업로드(putObject)
        amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));

        //업로드 후 URL 반환
        return amazonS3.getUrl(bucket, fileName).toString();
    }

}

