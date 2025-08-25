package com.realive.serviceimpl.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.common.enums.PaymentStatus;
import com.realive.domain.order.Order;
import com.realive.domain.payment.Payment;
import com.realive.dto.payment.TossPaymentApproveRequestDTO;
import com.realive.dto.payment.TossPaymentApproveResponseDTO;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.payment.PaymentRepository;
import com.realive.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${toss.secret-key:test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R}")
    private String tossSecretKey;
    
    @Value("${toss.mock-enabled:true}")
    private boolean mockEnabled;

    @Override
    @Transactional
    public Payment approveTossPayment(TossPaymentApproveRequestDTO request) {
        TossPaymentApproveResponseDTO tossResponse;
        
        // Mock 결제 처리
        if (mockEnabled) {
            logger.info("Mock 결제 처리 모드 - 실제 토스페이먼츠 API 호출 우회: {}", request);
            tossResponse = createMockTossResponse(request);
        } else {
            // 토스페이먼츠 API 호출 (운영)
            // 1. 토스 인증 헤더 생성
            String encodedAuth = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
            String authorization = "Basic " + encodedAuth;

            try {
                tossResponse = webClient.post()
                        .uri("/v2/payments/confirm")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(request))
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError(), this::handle4xxError)
                        .onStatus(status -> status.is5xxServerError(), this::handle5xxError)
                        .bodyToMono(TossPaymentApproveResponseDTO.class)
                        .block(); // 동기 처리
            } catch (ResponseStatusException e) {
                logger.error("토스페이먼츠 API 통신 중 HTTP 오류 발생: {}", e.getReason(), e);
                throw e;
            } catch (Exception e) {
                logger.error("토스페이먼츠 API 호출 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
                throw new RuntimeException("결제 승인 API 호출 중 오류가 발생했습니다.", e);
            }
        }

        // 2. 응답 검증
        if (tossResponse == null || !"DONE".equals(tossResponse.getStatus())) {
            logger.error("토스페이먼츠 결제 승인 응답이 유효하지 않거나 실패 상태입니다. 요청: {}, 응답: {}", request, tossResponse);
            throw new IllegalArgumentException("토스페이먼츠 결제 승인 응답이 유효하지 않거나 실패 상태입니다.");
        }

        // 3. 주문 조회
        Long ourOrderId;
        Order order;
        
        if (mockEnabled) {
            // Mock 모드에서는 orderId 파싱을 우회하고, 대신 결제 금액으로 주문을 찾음
            logger.info("Mock 모드: orderId 파싱 우회, 결제 금액 {}원으로 주문 조회 중...", tossResponse.getTotalAmount());
            
            // Mock 모드에서는 결제 금액을 기준으로 가장 최근 INIT 상태 주문을 찾음
            // 실제로는 주문 ID를 별도로 관리해야 하지만, 개발 환경에서는 이렇게 처리
            order = orderRepository.findAll().stream()
                    .filter(o -> o.getStatus() == OrderStatus.INIT)
                    .filter(o -> o.getTotalPrice() == tossResponse.getTotalAmount().intValue())
                    .max((o1, o2) -> o1.getOrderedAt().compareTo(o2.getOrderedAt())) // 가장 최근 주문
                    .orElseThrow(() -> new IllegalArgumentException("결제 금액 " + tossResponse.getTotalAmount() + "원과 일치하는 대기 중인 주문을 찾을 수 없습니다"));
            
            logger.info("Mock 모드: 주문 조회 성공 - 주문 ID: {}, 금액: {}원", order.getId(), order.getTotalPrice());
        } else {
            // 실제 모드에서는 기존 로직 사용
            try {
                ourOrderId = Long.parseLong(request.getOrderId());
            } catch (NumberFormatException e) {
                logger.error("Toss Payment에서 받은 orderId 형식이 올바르지 않습니다: {}", request.getOrderId(), e);
                throw new IllegalArgumentException("주문 ID 형식이 올바르지 않습니다.", e);
            }
            
            order = orderRepository.findById(ourOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다: " + ourOrderId));
        }

        // 4. 주문 상태 변경 및 실제 결제수단으로 업데이트
        order.setStatus(OrderStatus.PAYMENT_COMPLETED);
        // 토스페이먼츠에서 받은 실제 결제수단으로 업데이트 (데이터) 일관성 보장
        order.setPaymentMethod(tossResponse.getMethod());
        orderRepository.save(order);

        // 5. 결제 정보 저장
        Long amount = Objects.requireNonNull(tossResponse.getTotalAmount(), "토스 응답에 totalAmount가 누락되었습니다.");

        Payment payment = Payment.builder()
                .paymentKey(tossResponse.getPaymentKey())
                .order(order)
                .amount(amount)
                .balanceAmount(amount)
                .method(tossResponse.getMethod())
                .status(mapTossStatusToPaymentStatus(tossResponse.getStatus()))
                .requestedAt(Objects.requireNonNull(tossResponse.getRequestedAt(), "결제 요청 시간이 누락되었습니다."))
                .approvedAt(Objects.requireNonNull(tossResponse.getApprovedAt(), "결제 승인 시간이 누락되었습니다."))
                .type(tossResponse.getType())
                .customerKey(tossResponse.getCustomerKey())
                .currency(tossResponse.getCurrency())
                .lastTransactionKey(tossResponse.getLastTransactionKey())
                .rawResponseData(mockEnabled ? null : convertResponseToJson(tossResponse)) // Mock 모드에서만 null
                .build();

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문의 결제 정보를 찾을 수 없습니다. orderId: " + orderId));
    }

    private Mono<? extends Throwable> handle4xxError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    logger.error("Toss Payments Client Error ({}): {}", clientResponse.statusCode(), errorBody);
                    return Mono.error(new ResponseStatusException(
                            clientResponse.statusCode(),
                            "토스페이먼츠 클라이언트 오류: " + errorBody
                    ));
                });
    }

    private Mono<? extends Throwable> handle5xxError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    logger.error("Toss Payments Server Error ({}): {}", clientResponse.statusCode(), errorBody);
                    return Mono.error(new ResponseStatusException(
                            clientResponse.statusCode(),
                            "토스페이먼츠 서버 오류: " + errorBody
                    ));
                });
    }

    private String convertResponseToJson(TossPaymentApproveResponseDTO response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.error("TossPaymentApproveResponseDTO를 JSON으로 변환 실패: {}", response, e);
            return null;
        }
    }

    /**
     * Mock 토스페이먼츠 응답 생성 (개발 환경용)
     */
    private TossPaymentApproveResponseDTO createMockTossResponse(TossPaymentApproveRequestDTO request) {
        String mockMethod = "일반 결제"; // Mock 개발환경임을 명확히 표시
        
        logger.info("Mock 결제수단 설정: {} (개발환경 - 실제 환경에서는 사용자 선택에 따라 달라짐)", mockMethod);
        
        TossPaymentApproveResponseDTO response = new TossPaymentApproveResponseDTO();
        response.setPaymentKey(request.getPaymentKey());
        response.setOrderId(request.getOrderId());
        response.setTotalAmount(request.getAmount());
        response.setStatus("DONE");
        response.setRequestedAt(java.time.LocalDateTime.now());
        response.setApprovedAt(java.time.LocalDateTime.now());
        response.setMethod(mockMethod);
        response.setType("NORMAL");
        response.setCurrency("KRW");
        response.setCustomerKey(null);
        response.setLastTransactionKey(null);
        return response;
    }

    private PaymentStatus mapTossStatusToPaymentStatus(String tossStatus) {
        return switch (tossStatus) {
            case "READY" -> PaymentStatus.READY;
            case "IN_PROGRESS" -> PaymentStatus.IN_PROGRESS;
            case "WAITING_FOR_DEPOSIT" -> PaymentStatus.WAITING_FOR_DEPOSIT;
            case "DONE" -> PaymentStatus.COMPLETED;
            case "CANCELED" -> PaymentStatus.CANCELED;
            case "PARTIAL_CANCELED" -> PaymentStatus.PARTIAL_CANCELED;
            case "EXPIRED" -> PaymentStatus.FAILED;
            case "ABORTED" -> PaymentStatus.CANCELED;
            default -> {
                logger.warn("알 수 없는 토스 결제 상태: {}", tossStatus);
                yield PaymentStatus.FAILED;
            }
        };
    }
}
