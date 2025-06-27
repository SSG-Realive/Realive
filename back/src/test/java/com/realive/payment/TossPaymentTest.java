package com.realive.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TossPaymentTest {

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testCartPayment() {
        // 장바구니 결제 테스트
        String url = "http://localhost:8080/api/customer/cart/payment";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("YOUR_CUSTOMER_TOKEN");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", "test_payment_key_123");
        requestBody.put("tossOrderId", "test_order_123");
        requestBody.put("amount", 50000);
        requestBody.put("receiverName", "테스트");
        requestBody.put("phone", "010-1234-5678");
        requestBody.put("deliveryAddress", "서울시 강남구");
        requestBody.put("paymentMethod", "CARD");
        requestBody.put("customerId", 1);
        
        Map<String, Object> orderItem = new HashMap<>();
        orderItem.put("productId", 1);
        orderItem.put("quantity", 1);
        requestBody.put("orderItems", new Object[]{orderItem});

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.POST, request, String.class);
        
        System.out.println("Cart Payment Response: " + response.getBody());
    }

    @Test
    public void testDirectPayment() {
        // 직접 결제 테스트
        String url = "http://localhost:8080/api/customer/orders/direct-payment";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("YOUR_CUSTOMER_TOKEN");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", "test_payment_key_456");
        requestBody.put("tossOrderId", "test_order_456");
        requestBody.put("amount", 50000);
        requestBody.put("receiverName", "테스트");
        requestBody.put("phone", "010-1234-5678");
        requestBody.put("deliveryAddress", "서울시 강남구");
        requestBody.put("paymentMethod", "CARD");
        requestBody.put("customerId", 1);
        requestBody.put("productId", 1);
        requestBody.put("quantity", 1);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.POST, request, String.class);
        
        System.out.println("Direct Payment Response: " + response.getBody());
    }

    @Test
    public void testAuctionPayment() {
        // 경매 결제 테스트
        String url = "http://localhost:8080/api/customer/auctions/payment";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("YOUR_CUSTOMER_TOKEN");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("auctionId", 1);
        requestBody.put("paymentKey", "test_payment_key_789");
        requestBody.put("tossOrderId", "test_order_789");
        requestBody.put("paymentMethod", "CARD");
        requestBody.put("receiverName", "테스트");
        requestBody.put("phone", "010-1234-5678");
        requestBody.put("deliveryAddress", "서울시 강남구");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.POST, request, String.class);
        
        System.out.println("Auction Payment Response: " + response.getBody());
    }
} 