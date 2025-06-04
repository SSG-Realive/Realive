package com.realive.test.review;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.common.enums.MediaType;
import com.realive.domain.common.enums.ProductStatus;
import com.realive.domain.product.Product;
import com.realive.domain.product.ProductImage;
import com.realive.repository.review.view.ReviewViewRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
//@Log4j2
@Transactional
public class ReviewRepoTests {

//    @Autowired
//    private JPAQueryFactory queryFactory;
//
//    @Autowired(required = false)
//    private ReviewViewRepository reviewViewRepository;
//
//    @Autowired
//    private ProductViewRepository productRepository;
//
//    @Autowired
//    private EntityManager em;
//
//    @Test
//    @Rollback(false)
//    public void testInsertDummyData() {
//
//
//        for (int i = 0; i < 500; i++) {
//            Product product = Product.builder()
//                    .name("테스트 상품 " + i)
//                    .price(10000 * i)
//                    .status(ProductStatus.상)  // enum 상수를 넣어야 함
//                    .isActive(true)
//                    .build();
//
//            productRepository.save(product);
//
//            ProductImage thumbnail = ProductImage.builder()
//                    .product(product)
//                    .url("http://example.com/image" + i + ".jpg")
//                    .isThumbnail(true)
//                    .mediaType(MediaType.IMAGE)
//                    .build();
//
//            em.persist(thumbnail);
//        }
//
//        em.flush();
//        em.clear();
//
//
//        }

    }

