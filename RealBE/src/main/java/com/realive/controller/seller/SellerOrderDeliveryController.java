package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.dto.order.OrderDeliveryResponseDTO;
import com.realive.dto.order.OrderStatisticsDTO;
import com.realive.dto.seller.SellerOrderDetailResponseDTO;
import com.realive.security.seller.SellerPrincipal;
import com.realive.service.order.OrderDeliveryService;
import com.realive.service.order.SellerOrderService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/orders")
public class SellerOrderDeliveryController {

    private final OrderDeliveryService orderDeliveryService;
    private final SellerOrderService sellerOrderService;  // 추가

    // getOrderDetail 메서드에서 문제:
    @GetMapping("/{orderId}")
    public ResponseEntity<SellerOrderDetailResponseDTO> getOrderDetail(
            @PathVariable Long orderId,
            @AuthenticationPrincipal SellerPrincipal principal
    ) {
        if (principal == null) {
            throw new IllegalArgumentException("판매자 인증 정보가 없습니다.");
        }

        // ❌ 문제: SellerOrderService에 getOrderDetail 메서드가 없음
        SellerOrderDetailResponseDTO orderDetail = sellerOrderService.getOrderDetail(principal.getId(), orderId);
        return ResponseEntity.ok(orderDetail);
    }


    // PATCH /api/seller/orders/{orderId}/delivery
    @PatchMapping("/{orderId}/delivery")
    public ResponseEntity<Void> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestBody DeliveryStatusUpdateDTO dto,
            @AuthenticationPrincipal SellerPrincipal principal) {

        // ✅ 현재 로그인한 판매자 꺼내기



        Long sellerId = principal.getId();

        // ✅ sellerId 포함해서 서비스 호출
        orderDeliveryService.updateDeliveryStatus(sellerId, orderId, dto);

        return ResponseEntity.ok().build();
    }

    // 배송 단건 조회 컨트롤러
    @GetMapping("/{orderId}/delivery")
    public ResponseEntity<OrderDeliveryResponseDTO> getDeliveryByOrderId(@PathVariable Long orderId, @AuthenticationPrincipal SellerPrincipal principal) {

        
        OrderDeliveryResponseDTO result = orderDeliveryService.getDeliveryByOrderId(principal.getId(), orderId);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrderDelivery(
            @PathVariable Long orderId,
            @AuthenticationPrincipal SellerPrincipal principal
           ) {

        
        orderDeliveryService.cancelOrderDelivery(orderId, principal.getId());

        return ResponseEntity.ok().build();
    }

    /**
     * 판매자 주문 통계 조회
     * GET /api/seller/orders/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<OrderStatisticsDTO> getOrderStatistics(
            @AuthenticationPrincipal SellerPrincipal principal) {

        OrderStatisticsDTO statistics = sellerOrderService.getOrderStatistics(principal.getId());
        return ResponseEntity.ok(statistics);
    }

}