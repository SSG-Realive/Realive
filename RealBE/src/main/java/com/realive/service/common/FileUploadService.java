//package com.realive.service.common;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.UUID;
//
///**
// * MultipartFile을 서버의 로컬 디렉토리에 저장하고,
// * 저장된 경로를 문자열로 반환하는 파일 업로드 서비스
// */
//@Service
//@RequiredArgsConstructor
//public class FileUploadService {
//
//    private final S3Uploader s3Uploader;
//
//    /**
//     * S3에 업로드하고, 전체 URL 반환
//     * @param file Multipart 파일
//     * @param category 예: "product"
//     * @param sellerId 예: 5
//     * @return S3 URL
//     */
//    public String upload(MultipartFile file, String category, Long sellerId) {
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
//        }
//
//        try {
//            String path = category + "/" + sellerId;
//            return s3Uploader.upload(file, path);
//        } catch (IOException e) {
//            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
//        }
//    }
//
//    // 필요 시 삭제 기능도 구현 가능 (deleteObject, deleteFolder 등)
//}
//
//
//
////    // 실제 업로드 디렉토리 (프로젝트 외부 경로로 권장)
////    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
////
////    /**
////     * MultipartFile을 저장하고, 저장 경로 반환
////     * @param file 업로드할 이미지 파일
////     * @return 저장된 파일의 상대 경로 (/uploads/...)
////     */
////    public String upload(MultipartFile file, String category, Long sellerId ) {
////        if (file == null || file.isEmpty()) {
////            throw new RuntimeException("업로드할 파일이 존재하지 않습니다.");
////        }
////
////        // 고유한 파일명 생성
////        String originalFilename = file.getOriginalFilename();
////        String uuid = UUID.randomUUID().toString();
////        String newFilename = uuid + "_" + originalFilename;
////
////        // 업로드 디렉토리 없으면 생성
////        String subDirPath = category + "/" + sellerId + "/";
////        File uploadDir = new File(UPLOAD_DIR + subDirPath);
////        if (!uploadDir.exists()) {
////            uploadDir.mkdirs();
////        }
////
////        // 저장 위치
////        File dest = new File(UPLOAD_DIR + subDirPath + newFilename);
////
////        try {
////            file.transferTo(dest); // 파일 저장
////        } catch (IOException e) {
////            throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
////        }
////
////        // 나중에 프론트에서 이 경로를 통해 이미지 접근 가능
////        return "/uploads/" + subDirPath + newFilename;
////    }
////
////    /**
////     * 파일이 존재하면 삭제
////     * @param sellerId 판매자 ID
////     * @param category 파일 카테고리
////     */
////    public void deleteIfExists(Long sellerId, String category){
////
////        String folderPath = UPLOAD_DIR + category + "/" + sellerId + "/";
////        File folder = new File(folderPath);
////
////        if (folder.exists() && folder.isDirectory()){
////            File[] files = folder.listFiles();
////            if (files != null) {
////               for (File file : files) {
////                file.delete();
////               }
////            }
////        }
////        folder.delete();
////    }
//}