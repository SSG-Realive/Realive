//package com.realive.order;
//
//import com.realive.domain.common.enums.DeliveryStatus;
//import com.realive.domain.customer.Customer;
//import com.realive.domain.order.Order;
//import com.realive.domain.order.OrderDelivery;
//import com.realive.domain.product.Product;
//import com.realive.domain.seller.Seller;
//import com.realive.dto.order.DeliveryStatusUpdateDTO;
//import com.realive.dto.order.OrderDeliveryResponseDTO;
//import com.realive.repository.order.OrderDeliveryRepository;
//import com.realive.repository.order.SellerOrderDeliveryRepository;
//import com.realive.repository.order.OrderRepository;
//import com.realive.repository.product.ProductRepository;
//import com.realive.service.order.OrderDeliveryService;
//import com.realive.serviceimpl.order.OrderDeliveryServiceImpl;
//import jakarta.persistence.EntityManager;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Bean;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Transactional
//class OrderDeliveryServiceTest {
//
//    @Autowired
//    private OrderDeliveryService orderDeliveryService;
//
//    @Autowired
//    private SellerOrderDeliveryRepository sellerOrderDeliveryRepository;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private OrderDeliveryRepository orderDeliveryRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Autowired
//    private EntityManager em;
//
//    private Seller seller;
//    private Order order;
//    private OrderDelivery orderDelivery;
//
//    @BeforeEach
//    void setUp() {
//        // 판매자 생성
//        seller = Seller.builder()
//                .email("seller@test.com")
//                .name("테스트 판매자")
//                .build();
//
//        // 상품 생성
//        Product product = Product.builder()
//                .name("테스트 상품")
//                .seller(seller)
//                .build();
//        productRepository.save(product);
//
//        // 주문 생성
//        order = Order.builder()
//                .customer(Customer.builder().name("홍길동").build())
//                .build();
//        orderRepository.save(order);
//
//        // 배송 생성
//        orderDelivery = OrderDelivery.builder()
//                .order(order)
//                .status(DeliveryStatus.DELIVERY_PREPARING)
//                .build();
//        orderDeliveryRepository.save(orderDelivery);
//
//        em.flush();
//        em.clear();
//    }
//
//    @Test
//    void 배송_상태_조회() {
//        List<OrderDeliveryResponseDTO> list = orderDeliveryService.getDeliveriesBySeller(seller.getId());
//        assertThat(list).isNotEmpty();
//        assertThat(list.get(0).getDeliveryStatus()).isEqualTo(DeliveryStatus.DELIVERY_PREPARING);
//    }
//
//    @Test
//    void 배송_상태_변경() {
//        DeliveryStatusUpdateDTO dto = new DeliveryStatusUpdateDTO();
//        dto.setDeliveryStatus(DeliveryStatus.DELIVERY_IN_PROGRESS);
//        dto.setTrackingNumber("123456789");
//        dto.setCarrier("CJ택배");
//
//        orderDeliveryService.updateDeliveryStatus(seller.getId(), order.getId(), dto);
//
//        OrderDelivery updated = orderDeliveryRepository.findByOrder(order).orElseThrow();
//        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.DELIVERY_IN_PROGRESS);
//        assertThat(updated.getTrackingNumber()).isEqualTo("123456789");
//    }
//
//    @TestConfiguration
//    static class TestConfig {
//        @Bean
//        public OrderDeliveryService orderDeliveryService(
//                SellerOrderDeliveryRepository sellerOrderDeliveryRepository,
//                OrderDeliveryRepository orderDeliveryRepository
//        ) {
//            return new OrderDeliveryServiceImpl(sellerOrderDeliveryRepository, orderDeliveryRepository);
//        }
//    }
//}