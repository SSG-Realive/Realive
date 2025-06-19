package com.realive.serviceimpl.seller;

import com.realive.domain.common.enums.OrderStatus;
import com.realive.dto.seller.SellerDashboardResponseDTO;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.seller.SellerQnaRepository;
import com.realive.service.seller.SellerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerDashboardServiceImpl implements SellerDashboardService {

    private final ProductRepository productRepository;
    private final SellerQnaRepository sellerQnaRepository;
    private final OrderRepository orderRepository;

    @Override
    public SellerDashboardResponseDTO getDashboardInfo(Long sellerId) {
        // 오늘 날짜의 시작과 끝
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // 진행 중 주문 상태 정의
        List<OrderStatus> inProgressStatuses = List.of(
                OrderStatus.PAYMENT_COMPLETED,
                OrderStatus.DELIVERY_PREPARING,
                OrderStatus.DELIVERY_IN_PROGRESS
        );

        log.info("대시보드: sellerId={} 상품 개수 조회 시작", sellerId);
        long totalProductCount = productRepository.countBySellerIdAndActiveTrue(sellerId);
        log.info("대시보드: totalProductCount={}", totalProductCount);

        return SellerDashboardResponseDTO.builder()
                .totalProductCount(productRepository.countBySellerIdAndActiveTrue(sellerId))
                .unansweredQnaCount(sellerQnaRepository.countBySellerIdAndIsAnsweredFalseAndIsActiveTrue(sellerId))
                .todayProductCount(productRepository.countTodayProductBySellerId(sellerId, startOfDay, endOfDay))
                .totalQnaCount(sellerQnaRepository.countBySellerIdAndIsActiveTrue(sellerId))
                .inProgressOrderCount(orderRepository.countInProgressOrders(sellerId, inProgressStatuses))
                .build();
    }
}