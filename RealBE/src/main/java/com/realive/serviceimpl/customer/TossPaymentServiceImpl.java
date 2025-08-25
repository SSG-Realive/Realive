package com.realive.serviceimpl.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.dto.payment.TossPaymentResponse;
import com.realive.service.customer.TossPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentServiceImpl implements TossPaymentService {

    @Value("${toss.secret-key}")
    private String secretKey;

    private final ObjectMapper objectMapper;

    private static final String TOSS_API_BASE_URL = "https://api.tosspayments.com/v2/payments";

    @Override
    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, Long amount) {
        try {
            log.info("토스페이먼츠 v2 결제 승인 시작: paymentKey={}, orderId={}, amount={}", 
                    paymentKey, orderId, amount);

            String tossApiUrl = TOSS_API_BASE_URL + "/confirm";
            
            URL url = new URL(tossApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            
            // 토스페이먼츠 v2 인증 헤더 (secretKey + ":" 형식)
            String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + auth);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 요청 본문
            String requestBody = String.format(
                "{\"paymentKey\":\"%s\",\"orderId\":\"%s\",\"amount\":%d}",
                paymentKey, orderId, amount
            );

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 응답 처리
            int responseCode = connection.getResponseCode();
            log.info("토스페이먼츠 v2 API 응답 코드: {}", responseCode);

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 200 && responseCode < 300 
                                ? connection.getInputStream() 
                                : connection.getErrorStream(), 
                            StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            log.info("토스페이먼츠 v2 API 응답: {}", response.toString());

            if (responseCode == 200) {
                // 성공 응답 파싱
                Map<String, Object> responseMap = objectMapper.readValue(response.toString(), Map.class);
                
                return TossPaymentResponse.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .orderName((String) responseMap.get("orderName"))
                    .status((String) responseMap.get("status"))
                    .totalAmount(amount)
                    .method((String) responseMap.get("method"))
                    .approvedAt((String) responseMap.get("approvedAt"))
                    .requestedAt((String) responseMap.get("requestedAt"))
                    .message("결제가 성공적으로 완료되었습니다.")
                    .build();
            } else {
                // 실패 응답
                log.error("토스페이먼츠 v2 API 호출 실패: {}", response.toString());
                throw new RuntimeException("결제 승인 실패: " + response.toString());
            }

        } catch (Exception e) {
            log.error("토스페이먼츠 v2 결제 승인 처리 중 오류 발생", e);
            throw new RuntimeException("결제 승인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    public TossPaymentResponse getPayment(String paymentKey) {
        try {
            log.info("토스페이먼츠 결제 정보 조회: paymentKey={}", paymentKey);

            String tossApiUrl = TOSS_API_BASE_URL + "/" + paymentKey;
            
            URL url = new URL(tossApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            // 인증 헤더
            String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + auth);
            connection.setRequestProperty("Content-Type", "application/json");

            // 응답 처리
            int responseCode = connection.getResponseCode();
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 200 && responseCode < 300 
                                ? connection.getInputStream() 
                                : connection.getErrorStream(), 
                            StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            if (responseCode == 200) {
                Map<String, Object> responseMap = objectMapper.readValue(response.toString(), Map.class);
                
                return TossPaymentResponse.builder()
                    .paymentKey(paymentKey)
                    .orderId((String) responseMap.get("orderId"))
                    .orderName((String) responseMap.get("orderName"))
                    .status((String) responseMap.get("status"))
                    .totalAmount(((Number) responseMap.get("totalAmount")).longValue())
                    .method((String) responseMap.get("method"))
                    .approvedAt((String) responseMap.get("approvedAt"))
                    .requestedAt((String) responseMap.get("requestedAt"))
                    .message("결제 정보 조회 완료")
                    .build();
            } else {
                throw new RuntimeException("결제 정보 조회 실패: " + response.toString());
            }

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 정보 조회 중 오류 발생", e);
            throw new RuntimeException("결제 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    public TossPaymentResponse cancelPayment(String paymentKey, String cancelReason) {
        try {
            log.info("토스페이먼츠 결제 취소: paymentKey={}, reason={}", paymentKey, cancelReason);

            String tossApiUrl = TOSS_API_BASE_URL + "/" + paymentKey + "/cancel";
            
            URL url = new URL(tossApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            
            // 인증 헤더
            String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + auth);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 요청 본문
            String requestBody = String.format("{\"cancelReason\":\"%s\"}", cancelReason);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 응답 처리
            int responseCode = connection.getResponseCode();
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 200 && responseCode < 300 
                                ? connection.getInputStream() 
                                : connection.getErrorStream(), 
                            StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            if (responseCode == 200) {
                Map<String, Object> responseMap = objectMapper.readValue(response.toString(), Map.class);
                
                return TossPaymentResponse.builder()
                    .paymentKey(paymentKey)
                    .orderId((String) responseMap.get("orderId"))
                    .orderName((String) responseMap.get("orderName"))
                    .status((String) responseMap.get("status"))
                    .totalAmount(((Number) responseMap.get("totalAmount")).longValue())
                    .method((String) responseMap.get("method"))
                    .message("결제가 성공적으로 취소되었습니다.")
                    .build();
            } else {
                throw new RuntimeException("결제 취소 실패: " + response.toString());
            }

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 취소 중 오류 발생", e);
            throw new RuntimeException("결제 취소 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
} 