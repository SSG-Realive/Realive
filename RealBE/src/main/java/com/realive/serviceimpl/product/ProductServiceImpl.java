package com.realive.serviceimpl.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.domain.common.enums.MediaType;
import com.realive.domain.product.*;
import com.realive.domain.seller.Seller;
import com.realive.dto.admin.review.SellerRankingDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.*;
import com.realive.dto.seller.SellerPublicResponseDTO;
import com.realive.repository.product.*;
import com.realive.repository.review.SellerReviewRepository;
import com.realive.repository.seller.SellerRepository;
import com.realive.service.admin.logs.StatService;
import com.realive.service.common.S3Uploader;
import com.realive.service.product.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

        private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

        private final ProductImageRepository productImageRepository;
        private final ProductRepository productRepository;
        private final CategoryRepository categoryRepository;
        private final SellerRepository sellerRepository;
        private final DeliveryPolicyRepository deliveryPolicyRepository;
        private final StatService statService;
        private final SellerReviewRepository sellerReviewRepository; // SellerReviewRepository 주입
        private final S3Uploader s3Uploader;
        @Qualifier("openAiWebClient")
        private final WebClient openAiWebClient;  // 이름이 openAiWebClient인 빈 주입
        private final ObjectMapper objectMapper;

        @Value("${openai.api.key}")
        private String openAiApiKey;

        @Value("${openai.model}")
        private String model; // ex) gpt-4, gpt-3.5-turbo 등

        @Override
        public Long createProduct(ProductRequestDTO dto, Long sellerId) {
                Seller seller = sellerRepository.findById(sellerId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매자입니다."));

                Category category = null;
                if (dto.getCategoryId() != null) {
                        category = categoryRepository.findById(dto.getCategoryId())
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
                }

                Product product = Product.builder()
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .price(dto.getPrice())
                        .stock(dto.getStock() != null ? dto.getStock() : 1)
                        .width(dto.getWidth())
                        .depth(dto.getDepth())
                        .height(dto.getHeight())
                        .status(dto.getStatus())
                        .active(dto.getActive() != null ? dto.getActive() : true)
                        .category(category)
                        .seller(seller)
                        .build();

                productRepository.save(product);

                // 대표 이미지 저장 (S3)
                try {
                        String imageUrl = s3Uploader.upload(dto.getImageThumbnail(), "product/" + sellerId);
                        productImageRepository.save(ProductImage.builder()
                                .url(imageUrl)
                                .isThumbnail(true)
                                .mediaType(MediaType.IMAGE)
                                .product(product)
                                .build());
                } catch (IOException e) {
                        throw new RuntimeException("대표 이미지 업로드 실패", e);
                }

                // 대표 영상 저장 (선택, S3)
                if (dto.getVideoThumbnail() != null && !dto.getVideoThumbnail().isEmpty()) {
                        try {
                                String videoUrl = s3Uploader.upload(dto.getVideoThumbnail(), "product/" + sellerId);
                                productImageRepository.save(ProductImage.builder()
                                        .url(videoUrl)
                                        .isThumbnail(true)
                                        .mediaType(MediaType.VIDEO)
                                        .product(product)
                                        .build());
                        } catch (IOException e) {
                                throw new RuntimeException("대표 영상 업로드 실패", e);
                        }
                }

                //  서브 이미지 저장 (S3)
                if (dto.getSubImages() != null && !dto.getSubImages().isEmpty()) {
                        for (MultipartFile file : dto.getSubImages()) {
                                if (file != null && !file.isEmpty()) {
                                        try {
                                                String url = s3Uploader.upload(file, "product/" + sellerId);
                                                productImageRepository.save(ProductImage.builder()
                                                        .url(url)
                                                        .isThumbnail(false)
                                                        .mediaType(MediaType.IMAGE)
                                                        .product(product)
                                                        .build());
                                        } catch (IOException e) {
                                                throw new RuntimeException("서브 이미지 업로드 실패", e);
                                        }
                                }
                        }
                }

                //  배송 정책 저장
                if (dto.getDeliveryPolicy() != null) {
                        DeliveryPolicy policy = DeliveryPolicy.builder()
                                .type(dto.getDeliveryPolicy().getType())
                                .cost(dto.getDeliveryPolicy().getCost())
                                .regionLimit(dto.getDeliveryPolicy().getRegionLimit())
                                .product(product)
                                .build();
                        deliveryPolicyRepository.save(policy);
                }

                return product.getId();
        }


        @Override
        public void updateProduct(Long productId, ProductRequestDTO dto, Long sellerId) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

                if (!product.getSeller().getId().equals(sellerId)) {
                        throw new SecurityException("해당 상품에 대한 수정 권한이 없습니다.");
                }

                // 대표 이미지 저장
                if (dto.getImageThumbnail() != null && !dto.getImageThumbnail().isEmpty()) {
                        productImageRepository.findByProductId(productId).stream()
                                .filter(img -> img.isThumbnail() && img.getMediaType() == MediaType.IMAGE)
                                .forEach(productImageRepository::delete);

                        try {
                                String imageUrl = s3Uploader.upload(dto.getImageThumbnail(), "product/" + sellerId);
                                productImageRepository.save(ProductImage.builder()
                                        .url(imageUrl)
                                        .isThumbnail(true)
                                        .mediaType(MediaType.IMAGE)
                                        .product(product)
                                        .build());
                        } catch (IOException e) {
                                throw new RuntimeException("대표 이미지 업로드 실패", e);
                        }
                }

                // 대표 영상 저장
                if (dto.getVideoThumbnail() != null && !dto.getVideoThumbnail().isEmpty()) {
                        productImageRepository.findByProductId(productId).stream()
                                .filter(img -> img.isThumbnail() && img.getMediaType() == MediaType.VIDEO)
                                .forEach(productImageRepository::delete);

                        try {
                                String videoUrl = s3Uploader.upload(dto.getVideoThumbnail(), "product/" + sellerId);
                                productImageRepository.save(ProductImage.builder()
                                        .url(videoUrl)
                                        .isThumbnail(true)
                                        .mediaType(MediaType.VIDEO)
                                        .product(product)
                                        .build());
                        } catch (IOException e) {
                                throw new RuntimeException("대표 영상 업로드 실패", e);
                        }
                }

                // 서브 이미지 저장
                if (dto.getSubImages() != null && !dto.getSubImages().isEmpty()) {
                        for (MultipartFile file : dto.getSubImages()) {
                                if (file != null && !file.isEmpty()) {
                                        try {
                                                String url = s3Uploader.upload(file, "product/" + sellerId);
                                                productImageRepository.save(ProductImage.builder()
                                                        .url(url)
                                                        .isThumbnail(false)
                                                        .mediaType(MediaType.IMAGE)
                                                        .product(product)
                                                        .build());
                                        } catch (IOException e) {
                                                throw new RuntimeException("서브 이미지 업로드 실패", e);
                                        }
                                }
                        }
                }

                // 상품 정보 수정
                product.setName(dto.getName());
                product.setDescription(dto.getDescription());
                product.setPrice(dto.getPrice());
                product.setStock(dto.getStock() != null ? dto.getStock() : product.getStock());
                product.setWidth(dto.getWidth());
                product.setDepth(dto.getDepth());
                product.setHeight(dto.getHeight());
                product.setStatus(dto.getStatus());

                if (dto.getActive() != null && dto.getActive()) {
                        if (product.getStock() >= 1) {
                                product.setActive(true);
                        } else {
                                product.setActive(false);
                                log.warn("재고가 없는 상품은 활성화할 수 없습니다. productId={}", product.getId());
                        }
                } else if (dto.getActive() != null && !dto.getActive()) {
                        product.setActive(false);
                }

                if (dto.getCategoryId() != null) {
                        Category category = categoryRepository.findById(dto.getCategoryId())
                                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
                        product.setCategory(category);
                }

                if (dto.getDeliveryPolicy() != null) {
                        DeliveryPolicy policy = deliveryPolicyRepository.findByProduct(product)
                                .orElse(new DeliveryPolicy());

                        policy.setProduct(product);
                        policy.setType(dto.getDeliveryPolicy().getType());
                        policy.setCost(dto.getDeliveryPolicy().getCost());
                        policy.setRegionLimit(dto.getDeliveryPolicy().getRegionLimit());

                        deliveryPolicyRepository.save(policy);
                }

                productRepository.save(product);
        }


        @Override
        public void deleteProduct(Long productId, Long sellerId) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

                if (!product.getSeller().getId().equals(sellerId)) {
                        throw new SecurityException("해당 상품에 대한 삭제 권한이 없습니다.");
                }

                product.setActive(false);
        }

        // 상품 목록 조회 (판매자 전용)
        @Override
        public PageResponseDTO<ProductListDTO> getProductsBySeller(Long sellerId, ProductSearchCondition condition) {

                Page<Product> result = productRepository.searchProducts(condition, sellerId);
                List<Product> products = result.getContent();

                List<Long> productIds = products.stream()
                        .map(Product::getId)
                        .toList();

                List<Object[]> rows = productImageRepository.findThumbnailUrlsByProductIds(productIds, MediaType.IMAGE);
                Map<Long, String> imageMap = rows.stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (String) row[1]));

                List<ProductListDTO> dtoList = products.stream()
                        .map(product -> ProductListDTO.from(product, imageMap.get(product.getId())))
                        .collect(Collectors.toList());

                return PageResponseDTO.<ProductListDTO>withAll()
                        .pageRequestDTO(condition)
                        .dtoList(dtoList)
                        .total((int) result.getTotalElements())
                        .build();
        }

        @Override
        public ProductResponseDTO getProductDetail(Long productId, Long sellerId) {

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

                Category category = product.getCategory();
                if (!product.getSeller().getId().equals(sellerId)) {
                        throw new SecurityException("해당 상품에 대한 조회 권한이 없습니다.");
                }

                    // ✅ 이미지 여러 장 가져오기
    List<String> imageUrls = productImageRepository.findAllByProductId(productId)
            .stream()
            .map(ProductImage::getUrl)
            .collect(Collectors.toList());

                return ProductResponseDTO.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .stock(product.getStock())
                        .width(product.getWidth())
                        .depth(product.getDepth())
                        .height(product.getHeight())
                        .status(product.getStatus().name())
                        .isActive(product.isActive())
                        .imageThumbnailUrl(getThumbnailUrlByType(productId, MediaType.IMAGE))
                        .videoThumbnailUrl(getThumbnailUrlByType(productId, MediaType.VIDEO))
                        .categoryName(Category.getCategoryFullPath(product.getCategory()))
                        .categoryId(category.getId())
                        .parentCategoryId(category.getParent() != null ? category.getParent().getId() : null) // ✅ 추가
                        .sellerName(product.getSeller().getName())
                        .sellerId(product.getSeller().getId())
                        .imageUrls(imageUrls)
                        .build();
        }

        public String getThumbnailUrlByType(Long productId, MediaType mediaType) {
                return productImageRepository
                        .findFirstByProductIdAndIsThumbnailTrueAndMediaType(productId, mediaType)
                        .map(ProductImage::getUrl)
                        .orElse(null);
        }

        @Override
        public PageResponseDTO<ProductListDTO> getAllProductsForAdmin(ProductSearchCondition condition) {
                log.info("관리자용 전체 상품 목록 조회 - 조건: {}", condition);

                // QueryDSL 기반 커스텀 메서드 사용
                Page<Product> result = productRepository.searchProducts(condition, null); // sellerId 없이 전체 조회
                List<Product> products = result.getContent();

                // 상품 id 목록 추출
                List<Long> productIds = products.stream()
                        .map(Product::getId)
                        .toList();

                // 썸네일 이미지 매핑
                List<Object[]> rows = productImageRepository.findThumbnailUrlsByProductIds(productIds, MediaType.IMAGE);
                Map<Long, String> imageMap = rows.stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (String) row[1]));

                // DTO 변환
                List<ProductListDTO> dtoList = products.stream()
                        .map(product -> ProductListDTO.from(product, imageMap.get(product.getId())))
                        .collect(Collectors.toList());

                return PageResponseDTO.<ProductListDTO>withAll()
                        .pageRequestDTO(condition)
                        .dtoList(dtoList)
                        .total((int) result.getTotalElements())
                        .build();
        }

        /**
         * 추천용: 후보 상위 candidateSize명 중 sellersPick명 랜덤 추출,
         * 각 셀러당 productsPerSeller개 랜덤 상품을 뽑아 DTO로 반환
         */
        @Override
        public List<FeaturedSellerProductsResponseDTO> getFeaturedSellersWithProducts(
                int candidateSize,
                int sellersPick,
                int productsPerSeller,
                long minReviews) {
                // 1) 상위 후보 셀러 조회
                List<SellerRankingDTO> originalRankings = statService
                        .getRanking(minReviews, PageRequest.of(0, candidateSize))
                        .getContent();

                // 2) 랜덤 셔플 및 sellersPick 수만큼 선택
                List<SellerRankingDTO> rankings = new ArrayList<>(originalRankings);
                Collections.shuffle(rankings);
                List<SellerRankingDTO> picked = rankings.subList(0, Math.min(sellersPick, rankings.size()));

                // 3) 셀러별 랜덤 상품 + 썸네일 URL 매핑 → DTO
                return picked.stream()
                                .map(seller -> {
                                        // (a) 랜덤 상품 조회
                                        List<Product> prods = productRepository
                                                        .findRandomProductsBySellerId(seller.getSellerId(),
                                                                        productsPerSeller);

                                        // (b) 상품 ID 목록 생성
                                        List<Long> prodIds = prods.stream()
                                                        .map(Product::getId)
                                                        .collect(Collectors.toList());

                                        // (c) productImageRepository를 통해 URL 맵 생성
                                        List<Object[]> rows = productImageRepository
                                                        .findThumbnailUrlsByProductIds(prodIds, MediaType.IMAGE);
                                        Map<Long, String> urlMap = rows.stream()
                                                        .collect(Collectors.toMap(
                                                                        row -> (Long) row[0],
                                                                        row -> (String) row[1]));

                                        // (d) DTO 변환
                                        List<FeaturedProductSummaryResponseDTO> summaryList = prods.stream()
                                                        .map(p -> FeaturedProductSummaryResponseDTO.builder()
                                                                        .productId(p.getId())
                                                                        .name(p.getName())
                                                                        .price(p.getPrice())
                                                                        .imageThumbnailUrl(urlMap.get(p.getId()))
                                                                        .build())
                                                        .collect(Collectors.toList());

                                        return FeaturedSellerProductsResponseDTO.builder()
                                                        .sellerId(seller.getSellerId())
                                                        .sellerName(seller.getSellerName())
                                                        .products(summaryList)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        @Override
        public List<MonthlyProductRegistrationDTO> getMonthlyProductRegistrationStats(int months) {
                log.info("월별 상품 등록 통계 조회 - 개월 수: {}", months);

                // 현재 날짜로부터 지정된 개월 수만큼 이전 날짜 계산
                LocalDateTime endDate = LocalDateTime.now();
                LocalDateTime startDate = endDate.minusMonths(months);

                // Repository에서 월별 통계 조회
                List<Object[]> monthlyStats = productRepository.getMonthlyProductRegistrationStats(startDate, endDate);

                // Object[]를 DTO로 변환
                return monthlyStats.stream()
                        .map(stat -> {
                                Integer year = (Integer) stat[0];
                                Integer month = (Integer) stat[1];
                                Long countLong = (Long) stat[2];  // Long으로 받기
                                Integer count = countLong.intValue();  // Integer로 변환

                                YearMonth yearMonth = YearMonth.of(year, month);

                                return MonthlyProductRegistrationDTO.builder()
                                        .yearMonth(yearMonth)
                                        .count(count)
                                        .build();
                        })
                        .collect(Collectors.toList());
        }

        @Override
        public List<DailyProductRegistrationDTO> getDailyProductRegistrationStats(int days) {
                log.info("일별 상품 등록 통계 조회 - 일 수: {}", days);

                // 현재 날짜로부터 지정된 일 수만큼 이전 날짜 계산
                LocalDateTime endDate = LocalDateTime.now();
                LocalDateTime startDate = endDate.minusDays(days);

                // Repository에서 일별 통계 조회
                List<Object[]> dailyStats = productRepository.getDailyProductRegistrationStats(startDate, endDate);

                // Object[]를 DTO로 변환
                return dailyStats.stream()
                        .map(stat -> {
                                java.sql.Date sqlDate = (java.sql.Date) stat[0];
                                LocalDate date = sqlDate.toLocalDate();  // java.sql.Date를 LocalDate로 변환
                                Long countLong = (Long) stat[1];  // Long으로 받기
                                Integer count = countLong.intValue();  // Integer로 변환

                                return DailyProductRegistrationDTO.builder()
                                        .date(date)
                                        .count(count)
                                        .build();
                        })
                        .collect(Collectors.toList());
        }


        // productId로 sellerId 검색(고객용)
        @Override
        @Transactional(readOnly = true) // 읽기 전용 트랜잭션
        public Optional<Long> getSellerIdByProductId(Long productId) {
                log.info("상품 ID {} 에 대한 판매자 ID를 가져오려 시도 중입니다.", productId);
                // Product 엔티티가 Seller 객체를 직접 참조하므로, .getSeller().getId()를 사용합니다.
                // 이때, product.seller가 로드되어 있지 않거나 null일 수 있으므로 Optional.map을 통해 안전하게 접근합니다.
                return productRepository.findById(productId)
                        .map(Product::getSeller) // Product에서 Seller 객체를 가져옴
                        .map(Seller::getId);     // Seller 객체에서 ID를 가져옴
        }

        // productId로 sellerId를 검색한 다음 id로 sellerInfo 조회
        @Override
        @Transactional(readOnly = true)
        public Optional<SellerPublicResponseDTO> getPublicSellerInfoBySellerId(Long sellerId) {
                log.info("판매자 ID {} 에 대한 공개 판매자 정보를 가져옵니다.", sellerId);

                // 1. 판매자 조회
                Optional<Seller> sellerOptional = sellerRepository.findById(sellerId);

                if (sellerOptional.isEmpty()) {
                        log.warn("판매자 ID {} 를 찾을 수 없습니다.", sellerId);
                        return Optional.empty();
                }

                Seller seller = sellerOptional.get();

                // 2. 리뷰 통계 조회
                Double averageRating = sellerReviewRepository.getAverageRatingBySellerId(sellerId);
                Long totalReviews = sellerReviewRepository.countReviewsBySellerId(sellerId);

                double finalAvg = (averageRating != null) ? averageRating : 0.0;
                long finalCount = (totalReviews != null) ? totalReviews : 0L;

                // 3. DTO 변환
                return Optional.of(SellerPublicResponseDTO.builder()
                        .id(seller.getId())
                        .name(seller.getName())
                        // .profileImageUrl(seller.getProfileImageUrl()) // 필요 시 활성화
                        // .isApproved(seller.isApproved())               // 필요 시 활성화
                        .averageRating(finalAvg)
                        .totalReviews(finalCount)
                        .createdAt(seller.getCreatedAt())
                        .contactNumber(seller.getPhone())
                        .businessNumber(seller.getBusinessNumber())
                        .build());
        }

    @Override
    @Transactional(readOnly = true)
    public Optional<SellerPublicResponseDTO> getPublicSellerInfoByProductId(Long productId) {
        log.info("상품 ID {} 에 대한 판매자 공개 정보를 조회합니다.", productId);

        return getSellerIdByProductId(productId)
                .flatMap(this::getPublicSellerInfoBySellerId); // 이미 구현된 메서드 재사용
    }



                // 판매자가 존재하면 Seller 엔티티를 SellerPublicResponseDTO로 변환하여 반환
                return sellerOptional.map(seller -> {
                        log.info("판매자 ID {} 에 대한 공개 판매자 정보를 성공적으로 가져왔습니다.", seller.getId());

                        // 3. SellerReviewRepository를 사용하여 해당 판매자의 리뷰 정보 집계
                        // sellerReviewRepository에 정의된 메서드 활용
                        Double averageRating = sellerReviewRepository.getAverageRatingBySellerId(sellerId);
                        Long totalReviews = sellerReviewRepository.countReviewsBySellerId(sellerId);
                        log.info("totalReviews: {}", totalReviews);

                        // null 처리: 리뷰가 없을 경우 getAverageRatingBySellerId는 null을 반환할 수 있으므로 0.0으로 처리
                        double finalAverageRating = (averageRating != null) ? averageRating : 0.0;
                        long finalTotalReviews = (totalReviews != null) ? totalReviews : 0L;

                        return SellerPublicResponseDTO.builder()
                                .id(seller.getId())
                                .name(seller.getName())
                                //.profileImageUrl(seller.getProfileImage())
                                //.isApproved(seller.isApproved())
                                .averageRating(finalAverageRating)
                                .totalReviews(finalTotalReviews)
                                .createdAt(seller.getCreatedAt())
                                .contactNumber(seller.getPhone())
                                .businessNumber(seller.getBusinessNumber())
                                .build();
                });
        }

        @Override
        public GenerateProductDescriptionResponseDTO generateDescription(GenerateProductDescriptionRequestDTO request) {
                try {
                        // OpenAI 메시지 구성
                        Map<String, Object> body = Map.of(
                                "model", model,
                                "messages", List.of(
                                        Map.of("role", "system", "content", "당신은 상품 기획자입니다. 주어진 정보를 바탕으로 매력적인 상품 설명을 작성하세요."),
                                        Map.of("role", "user", "content", buildPrompt(request))
                                )
                        );

                        String responseJson = openAiWebClient.post()
                                .uri("/chat/completions")
                                .header("Authorization", "Bearer " + openAiApiKey)
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();

                        // 응답 파싱
                        JsonNode root = objectMapper.readTree(responseJson);
                        String aiText = root.path("choices").get(0).path("message").path("content").asText();

                        return new GenerateProductDescriptionResponseDTO(aiText);

                } catch (Exception e) {
                        throw new RuntimeException("AI 상품 설명 생성 실패: " + e.getMessage(), e);
                }
        }

        private String buildPrompt(GenerateProductDescriptionRequestDTO request) {
                return String.format("""
        아래 정보를 참고하여 소비자에게 어필할 수 있는 상품 설명을 작성해주세요.

        - 상품명: %s
        - 카테고리: %s
        - 표현 톤: %s
        - 기능 및 특징: %s
        """,
                        request.getProductName(),
                        request.getCategoryName(),
                        request.getTone(),
                        String.join(", ", request.getFeatures())
                );
        }
}
