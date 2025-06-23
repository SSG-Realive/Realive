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

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    @Override
    @Transactional
    public Payment approveTossPayment(TossPaymentApproveRequestDTO request) {
        // 1. 토스 인증 헤더 생성
        String encodedAuth = Base64.getEncoder().encodeToString((tossSecretKey).getBytes(StandardCharsets.UTF_8));
        String authorization = "Basic " + encodedAuth;

        TossPaymentApproveResponseDTO tossResponse;
        try {
            tossResponse = webClient.post()
                    .uri("/v1/payments/confirm")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), this::handle4xxError) // 수정된 부분
                    .onStatus(status -> status.is5xxServerError(), this::handle5xxError) // 수정된 부분
                    .bodyToMono(TossPaymentApproveResponseDTO.class)
                    .block(); // 동기 처리
        } catch (ResponseStatusException e) {
            logger.error("토스페이먼츠 API 통신 중 HTTP 오류 발생: {}", e.getReason(), e);
            throw e;
        } catch (Exception e) {
            logger.error("토스페이먼츠 API 호출 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("결제 승인 API 호출 중 오류가 발생했습니다.", e);
        }

        // 2. 응답 검증
        if (tossResponse == null ||
                !Objects.equals(tossResponse.getOrderId(), request.getOrderId()) ||
                !Objects.equals(tossResponse.getTotalAmount(), request.getAmount()) ||
                !"DONE".equals(tossResponse.getStatus())) {
            logger.error("토스페이먼츠 결제 승인 응답이 유효하지 않거나 실패 상태입니다. 요청: {}, 응답: {}", request, tossResponse);
            throw new IllegalArgumentException("토스페이먼츠 결제 승인 응답이 유효하지 않거나 실패 상태입니다.");
        }

        // 3. 주문 ID 파싱 및 조회
        Long ourOrderId;
        try {
            ourOrderId = Long.parseLong(request.getOrderId());
        } catch (NumberFormatException e) {
            logger.error("Toss Payment에서 받은 orderId 형식이 올바르지 않습니다: {}", request.getOrderId(), e);
            throw new IllegalArgumentException("주문 ID 형식이 올바르지 않습니다.", e);
        }

        Order order = orderRepository.findById(ourOrderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다: " + ourOrderId));

        // 4. 주문 상태 변경
        order.setStatus(OrderStatus.PAYMENT_COMPLETED);
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
                .rawResponseData(convertResponseToJson(tossResponse))
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
