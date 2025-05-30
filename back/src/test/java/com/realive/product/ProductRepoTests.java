package com.realive.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.common.enums.MediaType;
import com.realive.domain.common.enums.ProductStatus;
import com.realive.domain.product.Product;
import com.realive.domain.product.ProductImage;
import com.realive.dto.product.ProductListDTO;
import com.realive.repository.customer.productview.ProductSearch;
import com.realive.repository.customer.productview.ProductViewRepository;

import jakarta.persistence.EntityManager;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
@Transactional
public class ProductRepoTests {


    @Autowired(required = false)
    private ProductViewRepository productRepository;

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private EntityManager em;
    

    // @Test
    // public void testSearch1() {
    //     // 페이지 번호 0, 페이지 크기 5로 요청
    //     Pageable pageable = PageRequest.of(0,10);
        
    //     //페이징 조회
    //     List<ProductListDto> result = productRepository.productList(pageable);

    //     // 조회 결과 출력
    //     result.forEach(dto -> {
    //         System.out.println(dto.getId() + " | " + dto.getName() + " | " + dto.getPrice() + " | " + dto.getThumbnailUrl());
    //     });
    // }

 
    //더미데이터 추가해서 페이징 테스트
    // @Rollback(false) 
    // @Test
    // public void testSearchWithMultipleDummyData() {

    //     // 10개의 상품 + 썸네일 이미지 생성 및 저장
    //     for (int i = 1; i <= 15; i++) {
    //         Product product = Product.builder()
    //                 .name("테스트 상품 " + i)
    //                 .price(10000 * i)
    //                 .status(ProductStatus.상)  // enum 상수를 넣어야 함
    //                 .isActive(true)
    //                 .build();

    //         productRepository.save(product);

    //         ProductImage thumbnail = ProductImage.builder()
    //                 .product(product)
    //                 .url("http://example.com/image" + i + ".jpg")
    //                 .isThumbnail(true)
    //                 .mediaType(MediaType.IMAGE)
    //                 .build();

    //         em.persist(thumbnail);
    //     }

    //     em.flush();
    //     em.clear();

    //     Pageable pageable = PageRequest.of(0, 10);
    //     List<ProductListDto> result = productRepository.productList(pageable);

    //     result.forEach(dto -> {
    //         System.out.println(dto.getId() + " | " + dto.getName() + " | " + dto.getPrice() + " | " + dto.getThumbnailUrl());
    //     });

    //     assertEquals(10, result.size());
    // }
    
}
