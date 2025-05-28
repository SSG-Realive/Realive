//package com.realive.serviceimpl.seller;
//
//import com.realive.domain.order.Order;
//import com.realive.domain.order.OrderDelivery;
//import com.realive.domain.seller.Seller;
//import com.realive.dto.order.OrderListDTO;
//import com.realive.dto.order.OrderSearchCondition;
//import com.realive.repository.order.OrderRepository;
//import com.realive.repository.seller.SellerRepository;
//import com.realive.service.seller.SellerOrderService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class SellerOrderServiceImpl implements SellerOrderService {
//
//    private final SellerRepository sellerRepository;
//    private final OrderRepository orderRepository;
//
//    @Override
//    public Page<OrderListDTO> getOrdersBySeller(String email, OrderSearchCondition condition, Pageable pageable) {
//        Seller seller = sellerRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("판매자 정보가 없습니다."));
//
//        Sort.Direction direction = Sort.Direction.fromOptionalString(condition.getDirection()).orElse(Sort.Direction.DESC);
//        Pageable sortedPageable = PageRequest.of(
//                pageable.getPageNumber(),
//                pageable.getPageSize(),
//                Sort.by(direction, condition.getSort())
//        );
//
//        Page<Order> orders = orderRepository.findBySellerId(seller.getId(), sortedPageable);
//
//        return orders.map(order -> {
//            OrderDelivery delivery = order.getOrderDelivery();
//
//            return OrderListDTO.builder()
//                    .orderId(order.getId())
//                    .productName(order.getProduct().getName())
//                    .quantity(order.getQuantity())
//                    .totalPrice(order.getQuantity() * order.getProduct().getPrice())
//                    .orderDate(order.getCreatedAt())
//                    .orderStatus(order.getStatus()
//                    .deliveryStatus(delivery != null ? delivery.getDeliveryStatus().name() : null)
//                    .deliveryStartDate(delivery != null ? delivery.getStartDate() : null)
//                    .deliveryCompleteDate(delivery != null ? delivery.getCompleteDate() : null)
//                    .build();
//        });
//    }
//}