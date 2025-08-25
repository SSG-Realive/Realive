package com.realive.repository.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.dto.order.OrderStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.common.enums.DeliveryType;
import com.realive.domain.customer.QCustomer;
import com.realive.domain.order.QOrder;
import com.realive.domain.order.QOrderDelivery;
import com.realive.domain.order.QOrderItem;
import com.realive.domain.product.QProduct;
import com.realive.domain.seller.QSeller;
import com.realive.dto.order.SellerOrderListDTO;
import com.realive.dto.order.SellerOrderSearchCondition;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellerOrderDeliveryRepositoryImpl implements SellerOrderDeliveryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SellerOrderListDTO> getOrderListBySeller(
            Long sellerId,
            SellerOrderSearchCondition condition,
            Pageable pageable) {

        QOrderItem orderItem = QOrderItem.orderItem;
        QOrder order = QOrder.order;
        QProduct product = QProduct.product;
        QOrderDelivery delivery = QOrderDelivery.orderDelivery;
        QCustomer customer = QCustomer.customer;
        QSeller seller = QSeller.seller;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(seller.id.eq(sellerId));

        if (StringUtils.hasText(condition.getProductName())) {
            builder.and(product.name.containsIgnoreCase(condition.getProductName()));
        }

        if (condition.getDeliveryStatus() != null) {
            builder.and(delivery.status.eq(condition.getDeliveryStatus()));
        }

        if (condition.getFromDate() != null) {
            builder.and(order.orderedAt.goe(condition.getFromDate()));
        }

        if (condition.getToDate() != null) {
            builder.and(order.orderedAt.loe(condition.getToDate()));
        }

        // 실제 데이터 조회
        var results = queryFactory
                .select(Projections.constructor(SellerOrderListDTO.class,
                        order.id,
                        product.name,
                        customer.name,
                        orderItem.quantity,
                        delivery.status,
                        delivery.trackingNumber,
                        delivery.startDate,
                        delivery.completeDate,
                        order.orderedAt
                        
                ))
                .from(orderItem)
                .join(orderItem.order, order)
                .join(orderItem.product, product)
                .join(product.seller, seller)
                .join(order.customer, customer)
                .leftJoin(delivery).on(delivery.order.eq(order))
                .where(builder)
                .orderBy(order.orderedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        long total = queryFactory
                .select(order.count())
                .from(orderItem)
                .join(orderItem.order, order)
                .join(orderItem.product, product)
                .join(product.seller, seller)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public OrderStatisticsDTO getOrderStatisticsBySeller(Long sellerId) {
        QOrderItem orderItem = QOrderItem.orderItem;
        QOrder order = QOrder.order;
        QProduct product = QProduct.product;
        QOrderDelivery delivery = QOrderDelivery.orderDelivery;
        QSeller seller = QSeller.seller;

        // 🔥 단일 쿼리로 모든 통계 한 번에 계산
        var result = queryFactory
                .select(
                        order.count(),                                    // totalOrders
                        delivery.status.when(DeliveryStatus.DELIVERY_PREPARING).then(1L).otherwise(0L).sum(), // preparing
                        delivery.status.when(DeliveryStatus.DELIVERY_IN_PROGRESS).then(1L).otherwise(0L).sum(), // inProgress
                        delivery.status.when(DeliveryStatus.DELIVERY_COMPLETED).then(1L).otherwise(0L).sum()   // completed
                )
                .from(orderItem)
                .join(orderItem.order, order)
                .join(orderItem.product, product)
                .join(product.seller, seller)
                .leftJoin(delivery).on(delivery.order.eq(order))
                .where(seller.id.eq(sellerId))
                .fetchOne();

        return OrderStatisticsDTO.builder()
                .totalOrders(result.get(0, Long.class))
                .preparingOrders(result.get(1, Long.class))
                .inProgressOrders(result.get(2, Long.class))
                .completedOrders(result.get(3, Long.class))
                .build();
    }
}
