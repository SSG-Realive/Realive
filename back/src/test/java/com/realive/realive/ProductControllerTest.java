package com.realive.realive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.controller.product.ProductController;
import com.realive.security.JwtUtil;
import com.realive.service.product.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@Import({JwtUtil.class, ProductControllerTest.MockConfig.class})
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ProductService productService() {
            return Mockito.mock(ProductService.class);
        }
    }

    @Test
    @DisplayName("상품 등록 - 성공")
    @WithMockUser(username = "seller@example.com", roles = {"SELLER"})
    void registerProduct_Success() throws Exception {
        MockMultipartFile mainImage = new MockMultipartFile("mainImage", "main.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());
        MockMultipartFile subImage = new MockMultipartFile("subImages", "sub.jpg", MediaType.IMAGE_JPEG_VALUE, "sub".getBytes());
        MockMultipartFile name = new MockMultipartFile("name", "", "text/plain", "테스트 상품".getBytes());
        MockMultipartFile price = new MockMultipartFile("price", "", "text/plain", "10000".getBytes());

        mockMvc.perform(multipart("/api/seller/products")
                        .file(mainImage)
                        .file(subImage)
                        .file(name)
                        .file(price)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("상품 목록 조회 - 성공")
    @WithMockUser(roles = "SELLER")
    void getProducts_Success() throws Exception {
        mockMvc.perform(get("/api/seller/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 단건 조회 - 성공")
    @WithMockUser(roles = "SELLER")
    void getProductById_Success() throws Exception {
        mockMvc.perform(get("/api/seller/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 수정 - 성공")
    @WithMockUser(roles = "SELLER")
    void updateProduct_Success() throws Exception {
        MockMultipartFile name = new MockMultipartFile("name", "", "text/plain", "수정된 상품".getBytes());
        MockMultipartFile price = new MockMultipartFile("price", "", "text/plain", "15000".getBytes());

        mockMvc.perform(multipart("/api/seller/products/1")
                        .file(name)
                        .file(price)
                        .with(request -> {
                            request.setMethod("PUT"); // PUT 요청 강제
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 삭제 - 성공")
    @WithMockUser(roles = "SELLER")
    void deleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/api/seller/products/1"))
                .andExpect(status().isNoContent());

        // + 유효성 실패 테스트 , 권한 없는 요청

    }
}