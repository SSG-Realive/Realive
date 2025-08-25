package com.realive.util;

import com.realive.dto.chatbot.ChatRequestDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionSchemaFactory {

    // 특정 주문 상세 조회
    public static ChatRequestDTO.FunctionDefinition getOrderDetailFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "orderId", prop("integer", "주문 번호 ID")
                // "customerId" 제거됨
        ), List.of("orderId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getOrderDetail",
                "특정 주문 ID의 상세 정보를 조회합니다.",
                parameters
        );
    }

    // 주문 내역 조회
    public static ChatRequestDTO.FunctionDefinition getOrderListFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "limit", prop("integer", "조회할 최대 주문 개수 (기본값: 10)")
                // "customerId" 제거됨
        ), List.of()); // 필수 없음

        return new ChatRequestDTO.FunctionDefinition(
                "getOrderList",
                "사용자의 전체 주문 내역을 조회합니다.",
                parameters
        );
    }

    // 작성한 리뷰 목록 조회
    public static ChatRequestDTO.FunctionDefinition getReviewListFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                // "customerId" 제거됨 → 파라미터 없음
        ), List.of());

        return new ChatRequestDTO.FunctionDefinition(
                "getReviewList",
                "사용자가 작성한 리뷰 목록을 조회합니다.",
                parameters
        );
    }

    // 찜 목록 조회
    public static ChatRequestDTO.FunctionDefinition getWishlistFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                // "customerId" 제거됨 → 파라미터 없음
        ), List.of());

        return new ChatRequestDTO.FunctionDefinition(
                "getWishlistForCustomer",
                "사용자의 찜 목록을 조회합니다.",
                parameters
        );
    }

    // 상품 상세 정보 조회
    public static ChatRequestDTO.FunctionDefinition getProductInfoFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "productId", prop("integer", "상품 ID"),
                "sellerId", prop("integer", "판매자 ID")
        ), List.of("productId", "sellerId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getProductInfo",
                "특정 상품의 상세 정보를 조회합니다.",
                parameters
        );
    }

    // 판매자 정보 조회 (상품 기준)
    public static ChatRequestDTO.FunctionDefinition getPublicSellerInfoByProductIdFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "productId", prop("integer", "상품 ID")
        ), List.of("productId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getSellerInfoByProduct",
                "상품 ID를 기준으로 판매자 정보를 조회합니다.",
                parameters
        );
    }

    // 추천 셀러와 상품 조회
    public static ChatRequestDTO.FunctionDefinition getFeaturedSellersWithProductsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "candidateSize", prop("integer", "후보 셀러 수"),
                "sellersPick", prop("integer", "추천할 셀러 수"),
                "productsPerSeller", prop("integer", "셀러당 추천 상품 수"),
                "minReviews", prop("integer", "최소 리뷰 수 기준")
        ), List.of("candidateSize", "sellersPick", "productsPerSeller", "minReviews"));

        return new ChatRequestDTO.FunctionDefinition(
                "getFeaturedSellersWithProducts",
                "리뷰 수를 기준으로 추천 셀러와 추천 상품을 랜덤으로 조회합니다.",
                parameters
        );
    }

    // 상품 검색 (카테고리별, limit 개수)
    public static ChatRequestDTO.FunctionDefinition searchProductsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "limit", prop("integer", "조회할 최대 상품 개수 (기본값: 10)"),
                "categoryId", prop("integer", "카테고리 ID (선택 사항)")
        ), List.of()); // 필수 없음

        return new ChatRequestDTO.FunctionDefinition(
                "searchProducts",
                "카테고리 기준으로 최대 limit 개수만큼 상품을 조회합니다.",
                parameters
        );
    }

    // 관련 상품 조회 (특정 상품 기준)
    public static ChatRequestDTO.FunctionDefinition getRelatedProductsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "productId", prop("integer", "기준이 되는 상품 ID")
        ), List.of("productId")); // 필수

        return new ChatRequestDTO.FunctionDefinition(
                "getRelatedProducts",
                "특정 상품을 기준으로 관련 상품 목록을 조회합니다.",
                parameters
        );
    }

    // 인기 상품 조회 (파라미터 없음)
    public static ChatRequestDTO.FunctionDefinition getPopularProductsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                // 파라미터 없음
        ), List.of());

        return new ChatRequestDTO.FunctionDefinition(
                "getPopularProducts",
                "찜이 많은 인기 상품 목록을 조회합니다.",
                parameters
        );
    }

    public static ChatRequestDTO.FunctionDefinition getRecommendedProductsByCategoryFunction() {
        // 카테고리 설명 정의 (GPT가 categoryId를 의미 기반으로 선택할 수 있도록)
        String categoryIdDescription = String.join("\n", List.of(
                "다음 중 사용자 요청에 가장 적절한 카테고리 ID를 선택하세요:",
                "[거실 가구] (Parent: 10)",
                "11: 소파",
                "12: 거실 테이블",
                "13: TV·미디어장",
                "14: 진열장·책장",
                "",
                "[침실 가구] (Parent: 20)",
                "21: 침대",
                "22: 매트리스",
                "23: 화장대·거울",
                "24: 옷장·행거",
                "25: 수납장·서랍장",
                "",
                "[주방·다이닝 가구] (Parent: 30)",
                "31: 식탁",
                "32: 주방 의자",
                "33: 주방 수납장",
                "34: 아일랜드 식탁·홈바",
                "",
                "[서재·오피스 가구] (Parent: 40)",
                "41: 책상",
                "42: 사무용 의자",
                "43: 책장",
                "",
                "[기타] (Parent: 50)",
                "51: 현관·중문 가구",
                "52: 야외·아웃도어 가구 (예: 베란다, 테라스, 마당에서 사용하는 가구)",
                "53: 리퍼·전시가구",
                "54: DIY·부속품"
        ));

        // 파라미터 정의
        Map<String, Object> parameters = buildParameters(Map.of(
                "categoryId", prop("integer", categoryIdDescription),
                "limit", prop("integer", "추천 받을 상품 개수 (기본값: 3)")
        ), List.of("categoryId"));  // categoryId는 필수, limit은 선택

        // FunctionDefinition 반환
        return new ChatRequestDTO.FunctionDefinition(
                "getRecommendedProductsByCategory",
                "사용자의 요구와 가장 잘 맞는 가구 카테고리를 선택해 추천 상품을 제공합니다.",
                parameters
        );
    }

    public static ChatRequestDTO.FunctionDefinition getActiveAuctionsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "categoryFilter", prop("string", "조회할 경매 상품의 카테고리명 (예: '소파', '침대')"),
                "statusFilter", prop("string", "경매 상태 필터 (예: 'PROCEEDING', 'COMPLETED', 'SCHEDULED')"),
                "limit", prop("integer", "조회할 최대 경매 개수 (기본값: 10)")
        ), List.of()); // 모두 선택

        return new ChatRequestDTO.FunctionDefinition(
                "getActiveAuctions",
                "카테고리와 상태 필터를 기반으로 진행 중인 경매 목록을 조회합니다.",
                parameters
        );
    }

    public static ChatRequestDTO.FunctionDefinition getAuctionDetailsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "auctionId", prop("integer", "조회할 경매 ID")
        ), List.of("auctionId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getAuctionDetails",
                "특정 경매 ID의 상세 정보를 조회합니다.",
                parameters
        );
    }

    public static ChatRequestDTO.FunctionDefinition generateProductDescriptionFunction() {
        Map<String, Object> featuresProperty = new HashMap<>();
        featuresProperty.put("type", "array");
        featuresProperty.put("description", "상품 특징 리스트");
        featuresProperty.put("items", Map.of("type", "string"));

        Map<String, Map<String, String>> basicProps = Map.of(
                "productName", prop("string", "상품 이름"),
                "productBrand", prop("string", "상품 브랜드")
        );

        // 직접 Map<String, Object> 생성
        Map<String, Object> properties = new HashMap<>(basicProps);
        properties.put("productFeatures", featuresProperty); // 타입 불일치 감수하고 직접 넣음

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        parameters.put("required", List.of("productName"));

        return new ChatRequestDTO.FunctionDefinition(
                "generateProductDescription",
                "상품 설명을 생성합니다.",
                parameters
        );
    }



    // 모든 조회 가능한 함수 목록 반환
    public static List<ChatRequestDTO.FunctionDefinition> getAllFunctions() {
        return List.of(
                getOrderDetailFunction(),
                getOrderListFunction(),
                getReviewListFunction(),
                getWishlistFunction(),
                getPopularProductsFunction(),
                getProductInfoFunction(),
                getPublicSellerInfoByProductIdFunction(),
                getFeaturedSellersWithProductsFunction(),
                getRecommendedProductsByCategoryFunction(),
                getRelatedProductsFunction(),
                searchProductsFunction(),
                getActiveAuctionsFunction(),
                getAuctionDetailsFunction(),
                generateProductDescriptionFunction()
        );
    }
    // ====== 공통 로직 추출 ======
    private static Map<String, String> prop(String type, String desc) {
        Map<String, String> map = new HashMap<>();
        map.put("type", type);
        map.put("description", desc);
        return map;
    }

    private static Map<String, Object> buildParameters(Map<String, Map<String, String>> properties, List<String> required) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        parameters.put("required", required);
        return parameters;
    }
}
