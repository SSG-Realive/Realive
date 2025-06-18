package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.order.SellerOrderListDTO;
import com.realive.dto.order.SellerOrderSearchCondition;
import com.realive.service.order.SellerOrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    @GetMapping("/orders")
    public ResponseEntity<Page<SellerOrderListDTO>> getSellerOrders(
            @AuthenticationPrincipal Seller seller,
            SellerOrderSearchCondition condition,
            @PageableDefault(size = 10, sort = "orderedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        if (seller == null) {
            throw new IllegalArgumentException("판매자 인증 정보가 없습니다.");
        }

        Page<SellerOrderListDTO> orders = sellerOrderService.getOrderListBySeller(
                seller.getId(), condition, pageable
        );
        return ResponseEntity.ok(orders);
    }
}