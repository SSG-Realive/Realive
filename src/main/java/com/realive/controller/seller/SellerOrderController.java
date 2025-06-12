package com.realive.controller.seller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.domain.seller.Seller;
import com.realive.dto.order.SellerOrderListDTO;
import com.realive.dto.order.SellerOrderSearchCondition;
import com.realive.repository.seller.SellerRepository;
import com.realive.service.order.SellerOrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("/api/seller")
@RestController
@RequiredArgsConstructor
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;
    private final SellerRepository sellerRepository;

    @GetMapping("/orders")
    public ResponseEntity<Page<SellerOrderListDTO>> getSellerOrders(SellerOrderSearchCondition condition, Pageable pageable) {
    
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        Long sellerId = seller.getId();

        Page<SellerOrderListDTO> orders = sellerOrderService.getOrderListBySeller(sellerId, condition, pageable);
        return ResponseEntity.ok(orders);
    }
}
