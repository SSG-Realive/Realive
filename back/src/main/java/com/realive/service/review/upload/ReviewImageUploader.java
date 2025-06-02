package com.realive.service.review.upload;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Log4j2
public class ReviewImageUploader {

    @Value("${upload.dir}")
    private String UPLOAD_BASE_DIR;

    private static final String TEMP_DIR_NAME = "temp";
    private String TEMP_UPLOAD_DIR;

    public ReviewImageUploader(@Value("${upload.dir}") String uploadBaseDir) {
        this.UPLOAD_BASE_DIR = uploadBaseDir;
        this.TEMP_UPLOAD_DIR = UPLOAD_BASE_DIR + File.separator + TEMP_DIR_NAME;
        File tempDir = new File(TEMP_UPLOAD_DIR);
        if (!tempDir.exists()) {
            try {
                Files.createDirectories(tempDir.toPath());
                log.info("임시 업로드 디렉토리 생성 완료: {}", TEMP_UPLOAD_DIR);
            } catch (IOException e) {
                log.error("임시 업로드 디렉토리 생성 실패: {} - {}", TEMP_UPLOAD_DIR, e.getMessage(), e);
                throw new RuntimeException("임시 파일 업로드 디렉토리 생성에 실패했습니다.", e);
            }
        }
    }

    /**
     * 임시 디렉토리에 파일을 업로드하고 임시 파일의 경로를 반환합니다.
     * 이 경로는 리뷰 작성 API로 전달되어 최종 확정됩니다.
     *
     * @param file 업로드할 MultipartFile 객체
     * @return 임시 파일의 상대 URL (예: /uploads/temp/uuid_original.jpg)
     * @throws IllegalArgumentException 파일이 유효하지 않을 경우
     * @throws RuntimeException         파일 저장 중 예외 발생 시
     */
    public String uploadTemp(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("uploadTemp - 업로드할 파일이 존재하지 않거나 비어있습니다.");
            throw new IllegalArgumentException("업로드할 파일이 존재하지 않거나 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String newFilename = uuid + "_rv_" + originalFilename;

        Path destPath = Paths.get(TEMP_UPLOAD_DIR, newFilename);

        try {
            file.transferTo(destPath);
            log.info("uploadTemp - 임시 리뷰 이미지 파일 업로드 성공: filename={}, path={}", newFilename, destPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("uploadTemp - 임시 리뷰 이미지 파일 저장 중 오류 발생: filename={}, error={}", originalFilename, e.getMessage(), e);
            throw new RuntimeException("임시 파일 저장 중 오류가 발생했습니다. 다시 시도해주세요.", e);
        }

        return "/uploads/" + TEMP_DIR_NAME + "/" + newFilename;
    }

    /**
     * 임시 디렉토리에 있는 파일을 최종 리뷰 이미지 디렉토리로 이동시킵니다.
     * 이 메서드는 이벤트 핸들러의 트랜잭션 내에서 호출됩니다.
     *
     * @param tempImageUrl 임시 업로드 시 반환된 URL (예: /uploads/temp/uuid_original.jpg)
     * @param orderId      최종 저장될 주문 ID (경로 구성용)
     * @return 최종 저장된 파일의 URL (예: /uploads/12345/uuid_original.jpg)
     * @throws IOException      파일 이동 실패 시
     * @throws RuntimeException 파일 경로 파싱 오류 시
     */
    public String confirmImage(String tempImageUrl, Long orderId) throws IOException {
        if (tempImageUrl == null || !tempImageUrl.startsWith("/uploads/" + TEMP_DIR_NAME + "/")) {
            log.warn("confirmImage - 유효하지 않은 임시 이미지 URL 입니다: {}", tempImageUrl);
            throw new IllegalArgumentException("유효하지 않은 임시 이미지 URL 입니다.");
        }

        String filename = tempImageUrl.substring(("/uploads/" + TEMP_DIR_NAME + "/").length());
        Path sourcePath = Paths.get(TEMP_UPLOAD_DIR, filename);

        if (!Files.exists(sourcePath)) {
            log.error("confirmImage - 확정하려는 임시 파일이 존재하지 않습니다: {}", sourcePath);
            throw new IOException("확정하려는 임시 파일이 존재하지 않습니다.");
        }

        String finalSubDirPath = orderId + File.separator;
        String fullFinalDirPath = UPLOAD_BASE_DIR + File.separator + finalSubDirPath;

        File finalDir = new File(fullFinalDirPath);
        if (!finalDir.exists()) {
            try {
                Files.createDirectories(finalDir.toPath());
                log.info("confirmImage - 최종 리뷰 이미지 디렉토리 생성 완료: {}", fullFinalDirPath);
            } catch (IOException e) {
                log.error("confirmImage - 최종 리뷰 이미지 디렉토리 생성 실패: {} - {}", fullFinalDirPath, e.getMessage(), e);
                throw new IOException("최종 파일 디렉토리 생성에 실패했습니다.", e);
            }
        }

        Path destinationPath = Paths.get(fullFinalDirPath, filename);

        // ATOMIC_MOVE 옵션으로 원자적으로 이동 시도 (성공 아니면 실패)
        Files.move(sourcePath, destinationPath, StandardCopyOption.ATOMIC_MOVE);
        log.info("confirmImage - 임시 파일 -> 최종 파일 이동 성공: {} -> {}", sourcePath, destinationPath);

        return "/uploads/" + orderId + "/" + filename;
    }

    /**
     * 특정 임시 파일을 삭제합니다. (주로 이벤트 처리 실패 시 롤백 용도)
     * @param tempImageUrl 삭제할 임시 이미지 URL
     */
    public void deleteTempImage(String tempImageUrl) {
        if (tempImageUrl == null || !tempImageUrl.startsWith("/uploads/" + TEMP_DIR_NAME + "/")) {
            log.warn("deleteTempImage - 삭제하려는 임시 이미지 URL이 유효하지 않습니다: {}", tempImageUrl);
            return;
        }
        String filename = tempImageUrl.substring(("/uploads/" + TEMP_DIR_NAME + "/").length());
        Path filePath = Paths.get(TEMP_UPLOAD_DIR, filename);
        deleteFile(filePath);
    }

    /**
     * 최종 위치에 있는 파일을 삭제합니다. (주로 리뷰 삭제/수정 시)
     * @param finalImageUrl 삭제할 최종 이미지 URL
     */
    public void deleteFinalImage(String finalImageUrl) {
        if (finalImageUrl == null || !finalImageUrl.startsWith("/uploads/")) {
            log.warn("deleteFinalImage - 삭제하려는 최종 이미지 URL이 유효하지 않습니다: {}", finalImageUrl);
            return;
        }

        // /uploads/orderId/filename.jpg -> orderId/filename.jpg
        String relativePath = finalImageUrl.substring("/uploads/".length());
        Path filePath = Paths.get(UPLOAD_BASE_DIR, relativePath);
        deleteFile(filePath);
    }

    private void deleteFile(Path filePath) {
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("deleteFile - 파일 삭제 성공: {}", filePath);
            } else {
                log.warn("deleteFile - 삭제하려는 파일이 존재하지 않습니다: {}", filePath);
            }
        } catch (IOException e) {
            log.error("deleteFile - 파일 삭제 실패: {} - {}", filePath, e.getMessage(), e);
            // 파일 삭제 실패는 치명적이지 않을 수 있으므로 (스케줄러가 정리할 것임), 여기서 예외를 다시 던지지 않습니다.
        }
    }
}