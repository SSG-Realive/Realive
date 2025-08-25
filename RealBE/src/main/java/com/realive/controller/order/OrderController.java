package com.realive.controller.order;

import com.realive.dto.order.*;
import com.realive.security.customer.CustomerPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@Log4j2
public class OrderController {

    private final OrderService orderService;

    /**
     * 최근 주문 1건 미리보기 조회
     * GET /api/customer/orders/preview
     * @param userDetails 인증된 사용자 정보
     * @return OrderResponseDTO
     */
    @GetMapping("/preview")
    public ResponseEntity<OrderResponseDTO> getRecentOrder(
            @AuthenticationPrincipal CustomerPrincipal userDetails) {

        log.info("최근 주문 미리보기 요청 수신 - 고객 ID: {}", userDetails.getId());
        OrderResponseDTO recentOrder = orderService.getRecentOrder(userDetails.getId());

        return ResponseEntity.ok(recentOrder);
    }

    /**
     * 단일 상품 바로 구매 정보 조회
     * GET /api/customer/orders/direct-payment-info
     */
    @GetMapping("/direct-payment-info")
    public ResponseEntity<DirectPaymentInfoDTO> getDirectPaymentInfo(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {

        log.info("단일 상품 바로 구매 정보 조회 요청: productId={}, quantity={}", productId, quantity);
        DirectPaymentInfoDTO info = orderService.getDirectPaymentInfo(productId, quantity);
        return ResponseEntity.ok(info);
    }

    /**
     * 단일 상품 바로 구매 및 결제 처리
     * POST /api/customer/orders/direct-payment
     */
    @PostMapping("/direct-payment")
    public ResponseEntity<Long> processDirectPayment(
            @RequestBody PayRequestDTO payRequestDTO,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {

        log.info("단일 상품 바로 구매 요청 수신: {}", payRequestDTO);
        payRequestDTO.setCustomerId(userDetails.getId());
        Long orderId = orderService.processDirectPayment(payRequestDTO);
        log.info("단일 상품 주문 생성 완료 - 주문 ID: {}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED);
    }

    /**
     * 주문 상세 조회
     * GET /api/customer/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {

        log.info("주문 상세 조회 요청 - 주문 ID: {}, 고객 ID: {}", orderId, userDetails.getId());
        OrderResponseDTO order = orderService.getOrder(orderId, userDetails.getId());
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    /**
     * 주문 목록 조회
     * GET /api/customer/orders
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getOrderList(
            @PageableDefault(sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {

        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        log.info("주문 목록 조회 요청 - 페이지: {}, 고객 ID: {}", pageable.getPageNumber(), userDetails.getId());
        Page<OrderResponseDTO> orderList = orderService.getOrderList(pageable, userDetails.getId());
        return new ResponseEntity<>(orderList, HttpStatus.OK);
    }

    /**
     * 주문 취소
     * POST /api/customer/orders/cancel
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelOrder(
            @RequestBody OrderCancelRequestDTO orderCancelRequestDTO,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {

        log.info("주문 취소 요청: {}", orderCancelRequestDTO);
        orderCancelRequestDTO.setCustomerId(userDetails.getId());
        orderService.cancelOrder(orderCancelRequestDTO);
        log.info("주문 취소 완료 - 주문 ID: {}", orderCancelRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 구매 확정
     * POST /api/customer/orders/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmOrder(
            @RequestBody OrderConfirmRequestDTO orderConfirmRequestDTO,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {

        log.info("구매 확정 요청: {}", orderConfirmRequestDTO);
        orderConfirmRequestDTO.setCustomerId(userDetails.getId());
        orderService.confirmOrder(orderConfirmRequestDTO);
        log.info("구매 확정 완료 - 주문 ID: {}", orderConfirmRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 주문 삭제
     * DELETE /api/customer/orders
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteOrder(
            @RequestBody OrderDeleteRequestDTO orderDeleteRequestDTO,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {

        log.info("주문 삭제 요청: {}", orderDeleteRequestDTO);
        orderDeleteRequestDTO.setCustomerId(userDetails.getId());
        orderService.deleteOrder(orderDeleteRequestDTO);
        log.info("주문 삭제 완료 - 주문 ID: {}", orderDeleteRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
