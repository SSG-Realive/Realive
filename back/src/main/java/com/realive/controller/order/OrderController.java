package com.realive.controller.order;

import com.realive.dto.order.OrderAddRequestDTO;
import com.realive.dto.order.OrderResponseDTO;
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

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Log4j2
public class OrderController {

    private final OrderService orderService;

    /**
     * 새로운 주문을 생성합니다.
     * POST /api/orders
     * @param orderAddRequestDTO 주문 생성에 필요한 정보
     * @return 생성된 주문의 ID와 HTTP 상태 코드 201 (Created)
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderAddRequestDTO orderAddRequestDTO) {
        log.info("주문 생성 요청: {}", orderAddRequestDTO);
        Long orderId = orderService.createOrder(orderAddRequestDTO);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED);
    }

    /**
     * 특정 주문의 상세 정보를 조회합니다.
     * GET /api/orders/{orderId}?customerId={customerId}
     * @param orderId 조회할 주문의 ID
     * @param customerId 주문을 요청한 고객의 ID (주문 소유자 확인용)
     * @return 주문 상세 정보와 HTTP 상태 코드 200 (OK)
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(
            @PathVariable Long orderId,
            @RequestParam("customerId") Long customerId) {
        log.info("단일 주문 조회 요청 - 주문 ID: {}, 고객 ID: {}", orderId, customerId);
        OrderResponseDTO orderResponseDTO = orderService.getOrder(orderId, customerId);
        return new ResponseEntity<>(orderResponseDTO, HttpStatus.OK);
    }

    /**
     * 모든 주문 목록을 페이징하여 조회합니다.
     * GET /api/orders?page={page}&size={size}&sort={sort}
     * @param pageable 페이징 및 정렬 정보를 담는 객체
     * @return 주문 목록 페이지와 HTTP 상태 코드 200 (OK)
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @PageableDefault(size = 10, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("모든 주문 목록 조회 요청 - 페이지 정보: {}", pageable);
        Page<OrderResponseDTO> orderPage = orderService.getOrderList(pageable);
        return new ResponseEntity<>(orderPage, HttpStatus.OK);
    }

    // 예외 처리는 @ControllerAdvice를 통해 전역적으로 관리하는 것이 일반적입니다.
    // 여기서는 간결성을 위해 별도로 추가하지 않았습니다.
}