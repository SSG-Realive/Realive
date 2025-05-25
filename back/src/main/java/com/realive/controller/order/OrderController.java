package com.realive.controller.order;

import com.realive.dto.order.OrderAddRequestDTO;
import com.realive.dto.order.OrderDeleteRequestDTO;
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

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Log4j2
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderAddRequestDTO orderAddRequestDTO) {
        log.info("주문 생성 요청: {}", orderAddRequestDTO);
        Long orderId = orderService.createOrder(orderAddRequestDTO);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(
            @PathVariable Long orderId,
            @RequestParam("customerId") Long customerId) {
        log.info("단일 주문 조회 요청 - 주문 ID: {}, 고객 ID: {}", orderId, customerId);
        OrderResponseDTO orderResponseDTO = orderService.getOrder(orderId, customerId);
        return new ResponseEntity<>(orderResponseDTO, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @PageableDefault(size = 10, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("모든 주문 목록 조회 요청 - 페이지 정보: {}", pageable);
        Page<OrderResponseDTO> orderPage = orderService.getOrderList(pageable);
        return new ResponseEntity<>(orderPage, HttpStatus.OK);
    }


    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long orderId,
            @RequestBody OrderDeleteRequestDTO orderDeleteRequestDTO) {
        if (!orderId.equals(orderDeleteRequestDTO.getOrderId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 불일치 시 400 Bad Request
        }

        log.info("주문 삭제 요청 - 주문 ID: {}, 고객 ID: {}", orderDeleteRequestDTO.getOrderId(), orderDeleteRequestDTO.getCustomerId());
        try {
            orderService.deleteOrder(orderDeleteRequestDTO);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 성공적으로 삭제 시 204 No Content
        } catch (NoSuchElementException e) {
            log.warn("삭제하려는 주문을 찾을 수 없음: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        } catch (IllegalStateException e) {
            log.warn("주문 삭제 불가 (상태 오류): {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.CONFLICT); // 409 Conflict (주문 상태 때문에 삭제 불가)
        } catch (Exception e) {
            log.error("주문 삭제 중 오류 발생: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

    // 예외 처리는 @ControllerAdvice를 통해 전역적으로 관리하는 것이 일반적입니다.
    // 위 deleteOrder 메서드에만 임시로 포함했습니다.
}