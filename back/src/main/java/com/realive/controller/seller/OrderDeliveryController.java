package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.service.order.OrderDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/orders")
public class OrderDeliveryController {

    private final OrderDeliveryService orderDeliveryService;

    // PATCH /api/seller/orders/{orderId}/delivery
    @PatchMapping("/{orderId}/delivery")
    public ResponseEntity<Void> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestBody DeliveryStatusUpdateDTO dto) {

        // ✅ 현재 로그인한 판매자 꺼내기
        Seller seller = (Seller) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Long sellerId = seller.getId();

        // ✅ sellerId 포함해서 서비스 호출
        orderDeliveryService.updateDeliveryStatus(sellerId, orderId, dto);

        return ResponseEntity.ok().build();
    }
}
