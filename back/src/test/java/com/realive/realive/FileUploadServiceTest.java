package com.realive.realive;

import com.realive.service.common.FileUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileUploadServiceTest {

    private final FileUploadService fileUploadService = Mockito.mock(FileUploadService.class);

    @Test
    @DisplayName("파일 업로드 성공")
    void uploadFile_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "fake image content".getBytes());

        Mockito.when(fileUploadService.upload(file, "product", 1L))
                .thenReturn("https://fake.url/image.jpg");

        String result = fileUploadService.upload(file, "product", 1L);
        assertEquals("https://fake.url/image.jpg", result);
    }

    @Test
    @DisplayName("파일 업로드 실패 - 빈 파일")
    void uploadFile_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadService.upload(file, "product", 1L);
        });
    }
}