package com.realive.controller.order;

import com.realive.dto.order.*;
import com.realive.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

// JWT/OAuth2를 통한 사용자 인증이 구현되어 있다면,
// @AuthenticationPrincipal 또는 SecurityContextHolder를 통해 customerId를 가져올 수 있습니다.


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Log4j2
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성 및 결제 처리 (통합)
     * POST /api/orders/pay
     * @param payRequestDTO 결제 및 주문 생성 요청 DTO
     * @return 생성된 주문의 ID (Long)
     */
    @PostMapping("/payment")
    public ResponseEntity<Long> processPayment(@RequestBody PayRequestDTO payRequestDTO) {
        log.info("결제 및 주문 생성 요청 수신: {}", payRequestDTO);
        Long orderId = orderService.processPayment(payRequestDTO);
        log.info("주문이 성공적으로 생성되었습니다. 주문 ID: {}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED); // 201 Created
    }

    /**
     * 특정 주문 상세 조회
     * GET /api/orders/{orderId}?customerId={customerId} (또는 인증 정보 사용)
     * @param orderId 조회할 주문 ID
     * @param customerId 주문 소유자 고객 ID (인증 정보에서 가져오는 것이 권장됨)
     * @return OrderResponseDTO
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long orderId,
                                                     @RequestParam Long customerId) { // 실제 앱에서는 @AuthenticationPrincipal CustomerDetails customerrId 등을 사용
        log.info("주문 상세 조회 요청 수신: 주문 ID {}, 고객 ID {}", orderId, customerId);
        OrderResponseDTO order = orderService.getOrder(orderId, customerId);
        return new ResponseEntity<>(order, HttpStatus.OK); // 200 OK
    }

    /**
     * 주문 목록 조회 (페이징 지원)
     * GET /api/orders?page=0&size=10&sort=orderedAt,desc
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 기준)
     * @return Page<OrderResponseDTO>
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getOrderList(
            @PageableDefault(sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("주문 목록 조회 요청 수신: 페이지 {}, 크기 {}, 정렬 {}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<OrderResponseDTO> orderList = orderService.getOrderList(pageable);
        return new ResponseEntity<>(orderList, HttpStatus.OK); // 200 OK
    }

    /**
     * 주문 취소
     * POST /api/orders/cancel
     * @param orderCancelRequestDTO 취소할 주문 정보 (orderId, customerId, reason)
     * @return 응답 없음 (204 No Content)
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelOrder(@RequestBody OrderCancelRequestDTO orderCancelRequestDTO) {
        log.info("주문 취소 요청 수신: {}", orderCancelRequestDTO);
        orderService.cancelOrder(orderCancelRequestDTO);
        log.info("주문 취소 처리 완료: 주문 ID {}", orderCancelRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    /**
     * 구매 확정
     * POST /api/orders/confirm
     * @param orderConfirmRequestDTO 구매 확정할 주문 정보 (orderId, customerId)
     * @return 응답 없음 (204 No Content)
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmOrder(@RequestBody OrderConfirmRequestDTO orderConfirmRequestDTO) {
        log.info("구매 확정 요청 수신: {}", orderConfirmRequestDTO);
        orderService.confirmOrder(orderConfirmRequestDTO);
        log.info("구매 확정 처리 완료: 주문 ID {}", orderConfirmRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    /**
     * 주문 삭제
     * DELETE /api/orders
     * @param orderDeleteRequestDTO 삭제할 주문 정보 (orderId, customerId)
     * @return 응답 없음 (204 No Content)
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteOrder(@RequestBody OrderDeleteRequestDTO orderDeleteRequestDTO) {
        log.info("주문 삭제 요청 수신: {}", orderDeleteRequestDTO);
        orderService.deleteOrder(orderDeleteRequestDTO);
        log.info("주문 삭제 처리 완료: 주문 ID {}", orderDeleteRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    // 예외 처리 (선택 사항이지만 권장)
    // @ControllerAdvice 또는 각 Controller 내부에 @ExceptionHandler를 사용하여 예외를 처리할 수 있습니다.
    @ExceptionHandler({IllegalArgumentException.class, NoSuchElementException.class, IllegalStateException.class})
    public ResponseEntity<String> handleOrderExceptions(RuntimeException ex) {
        log.error("주문 처리 중 예외 발생: {}", ex.getMessage());
        // 각 예외 타입에 따라 다른 HTTP 상태 코드를 반환할 수 있습니다.
        if (ex instanceof IllegalArgumentException) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // 400 Bad Request
        } else if (ex instanceof NoSuchElementException) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
        } else if (ex instanceof IllegalStateException) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // 409 Conflict (비즈니스 로직 위반)
        }
        return new ResponseEntity<>("알 수 없는 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
    }
}