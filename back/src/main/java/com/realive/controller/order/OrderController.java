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
import com.realive.dto.customer.member.MemberLoginDTO;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@Log4j2
public class OrderController {

    private final OrderService orderService;

    /**
     * **단일 상품 바로 구매 정보 조회**
     * GET /api/customer/orders/direct-payment-info
     * @param productId 상품 ID
     * @param quantity 수량
     * @param userDetails 현재 인증된 사용자 정보
     * @return DirectPaymentInfoDTO
     */
    @GetMapping("/direct-payment-info")
    public ResponseEntity<DirectPaymentInfoDTO> getDirectPaymentInfo(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {
        log.info("단일 상품 바로 구매 정보 조회 요청: productId={}, quantity={}", productId, quantity);
        // 인증된 사용자 ID를 여기서 직접 사용하거나, PayRequestDTO에 포함시켜 서비스 계층으로 전달 가능
        // 현재 getDirectPaymentInfo는 customerId를 받지 않으므로, 이 정보는 결제 요청 시에 사용될 것입니다.
        DirectPaymentInfoDTO info = orderService.getDirectPaymentInfo(productId, quantity);
        return ResponseEntity.ok(info);
    }

    /**
     * **단일 상품 바로 구매 및 결제 처리**
     * POST /api/customer/orders/direct-payment
     * @param payRequestDTO 결제 및 주문 생성 요청 DTO (productId, quantity 필드 필수)
     * @param userDetails 현재 인증된 사용자 정보
     * @return 생성된 주문의 ID (Long)
     */
    @PostMapping("/direct-payment")
    public ResponseEntity<Long> processDirectPayment(
            @RequestBody PayRequestDTO payRequestDTO,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {
        log.info("단일 상품 바로 구매 요청 수신: {}", payRequestDTO);
        // 인증된 사용자 ID를 PayRequestDTO에 설정
        payRequestDTO.setCustomerId(userDetails.getId());
        Long orderId = orderService.processDirectPayment(payRequestDTO);
        log.info("단일 상품 주문이 성공적으로 생성되었습니다. 주문 ID: {}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED); // 201 Created
    }

    // --- `processCartPayment` 엔드포인트는 CartController로 이동되었습니다. ---

    /**
     * 특정 주문 상세 조회
     * GET /api/customer/orders/{orderId}
     * @param orderId 조회할 주문 ID
     * @param userDetails 현재 인증된 사용자 정보
     * @return OrderResponseDTO
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomerPrincipal userDetails) { // @RequestParam customerId 대신 @AuthenticationPrincipal 사용
        log.info("주문 상세 조회 요청 수신: 주문 ID {}, 고객 ID {}", orderId, userDetails.getId());
        OrderResponseDTO order = orderService.getOrder(orderId, userDetails.getId()); // userDetails에서 customerId 가져옴
        return new ResponseEntity<>(order, HttpStatus.OK); // 200 OK
    }

    /**
     * 주문 목록 조회 (페이징 지원)
     * GET /api/customer/orders?page=0&size=10&sort=orderedAt,desc
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 기준)
     * @return Page<OrderResponseDTO>
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getOrderList(
            @PageableDefault(sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomerPrincipal userDetails) { // 고객 ID를 받기 위해 추가
        
        
        if (userDetails == null) {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        
        log.info("주문 목록 조회 요청 수신: 페이지 {}, 크기 {}, 정렬 {}, 고객 ID {}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), userDetails.getId());
        Long customerId = userDetails.getId();
       
        Page<OrderResponseDTO> orderList = orderService.getOrderList(pageable, customerId); // 필요에 따라 customerId를 추가하도록 서비스 계층 수정 고려
        return new ResponseEntity<>(orderList, HttpStatus.OK); // 200 OK
    }

    /**
     * 주문 취소
     * POST /api/customer/orders/cancel
     * @param orderCancelRequestDTO 취소할 주문 정보 (orderId, reason)
     * @param userDetails 현재 인증된 사용자 정보
     * @return 응답 없음 (204 No Content)
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelOrder(
            @RequestBody OrderCancelRequestDTO orderCancelRequestDTO,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {
        log.info("주문 취소 요청 수신: {}", orderCancelRequestDTO);
        orderCancelRequestDTO.setCustomerId(userDetails.getId()); // 고객 ID 설정
        orderService.cancelOrder(orderCancelRequestDTO);
        log.info("주문 취소 처리 완료: 주문 ID {}", orderCancelRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    /**
     * 구매 확정
     * POST /api/customer/orders/confirm
     * @param orderConfirmRequestDTO 구매 확정할 주문 정보 (orderId)
     * @param userDetails 현재 인증된 사용자 정보
     * @return 응답 없음 (204 No Content)
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmOrder(
            @RequestBody OrderConfirmRequestDTO orderConfirmRequestDTO,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {
        log.info("구매 확정 요청 수신: {}", orderConfirmRequestDTO);
        orderConfirmRequestDTO.setCustomerId(userDetails.getId()); // 고객 ID 설정
        orderService.confirmOrder(orderConfirmRequestDTO);
        log.info("구매 확정 처리 완료: 주문 ID {}", orderConfirmRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    /**
     * 주문 삭제
     * DELETE /api/customer/orders
     * @param orderDeleteRequestDTO 삭제할 주문 정보 (orderId)
     * @param userDetails 현재 인증된 사용자 정보
     * @return 응답 없음 (204 No Content)
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteOrder(
            @RequestBody OrderDeleteRequestDTO orderDeleteRequestDTO,
            @AuthenticationPrincipal CustomerPrincipal userDetails) {
        log.info("주문 삭제 요청 수신: {}", orderDeleteRequestDTO);
        orderDeleteRequestDTO.setCustomerId(userDetails.getId()); // 고객 ID 설정
        orderService.deleteOrder(orderDeleteRequestDTO);
        log.info("주문 삭제 처리 완료: 주문 ID {}", orderDeleteRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }
}