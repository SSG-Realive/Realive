package com.realive.service.product;

import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.CustomerProductSearchCondition;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductRequestDTO;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.dto.product.ProductSearchCondition;
import com.realive.dto.product.MonthlyProductRegistrationDTO;
import com.realive.dto.product.DailyProductRegistrationDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.LocalDate;

public interface ProductService {

    /**
     * 상품 등록
     * - imageThumbnail: 대표 이미지 (필수)
     * - videoThumbnail: 대표 영상 (선택)
     */
    Long createProduct(ProductRequestDTO dto, Long sellerId);

    /**
     * 상품 수정
     * - 기존 썸네일 이미지/영상 모두 삭제 후 새로 저장
     */
    void updateProduct(Long productId, ProductRequestDTO dto, Long sellerId);

    /**
     * 상품 삭제 (이미지, 배송정책 포함 삭제)
     */
    void deleteProduct(Long productId, Long sellerId);

    /**
     * 판매자 ID 기준 상품 목록 조회(판매자용)
     * - imageThumbnailUrl / videoThumbnailUrl 포함
     */
    PageResponseDTO<ProductListDTO> getProductsBySeller(Long sellerId, ProductSearchCondition condition);

    /**
     * 상품 상세 조회
     * - imageThumbnailUrl / videoThumbnailUrl 포함
     */
    ProductResponseDTO getProductDetail(Long productId, Long sellerId);

    /**
     * 관리자용 전체 상품 목록 조회
     * - 모든 판매자의 상품을 조회
     * - 필터링: 카테고리, 상태, 활성화 여부, 가격 범위, 키워드 검색
     */
    PageResponseDTO<ProductListDTO> getAllProductsForAdmin(ProductSearchCondition condition);

    /**
     * 월별 상품 등록 통계 조회
     * - 지정된 기간 동안의 월별 상품 등록 수를 조회
     * - 관리자 대시보드용
     */
    List<MonthlyProductRegistrationDTO> getMonthlyProductRegistrationStats(int months);

    /**
     * 일별 상품 등록 통계 조회
     * - 지정된 기간 동안의 일별 상품 등록 수를 조회
     * - 관리자 대시보드용
     */
    List<DailyProductRegistrationDTO> getDailyProductRegistrationStats(int days);
}