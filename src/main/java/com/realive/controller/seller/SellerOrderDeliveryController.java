package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.dto.order.OrderDeliveryResponseDTO;
import com.realive.service.order.OrderDeliveryService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/orders")
public class SellerOrderDeliveryController {

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

    // 배송 단건 조회 컨트롤러
    @GetMapping("/{orderId}/delivery")
    public ResponseEntity<OrderDeliveryResponseDTO> getDeliveryByOrderId(@PathVariable Long orderId) {

        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OrderDeliveryResponseDTO result = orderDeliveryService.getDeliveryByOrderId(seller.getId(), orderId);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrderDelivery(
            @PathVariable Long orderId
           ) {

        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        orderDeliveryService.cancelOrderDelivery(orderId, seller.getId());

        return ResponseEntity.ok().build();
    }

}