package com.realive.controller.seller;

import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.service.order.OrderDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 판매자용 배송 상태 변경 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/orders")
public class OrderDeliveryController {

    private final OrderDeliveryService orderDeliveryService;

    /**
     * 배송 상태 업데이트 엔드포인트
     * PATCH /api/seller/orders/{orderId}/delivery
     */
    @PatchMapping("/{orderId}/delivery")
    public ResponseEntity<Void> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestBody DeliveryStatusUpdateDTO dto) {

        orderDeliveryService.updateDeliveryStatus(orderId, dto);
        return ResponseEntity.ok().build();
    }
}
