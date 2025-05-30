package com.realive.util;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploader {
    String uploadImage(MultipartFile file);
    String uploadImage(MultipartFile file, String prefix);
}