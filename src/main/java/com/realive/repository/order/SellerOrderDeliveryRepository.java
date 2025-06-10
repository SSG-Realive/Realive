package com.realive.repository.order;

import com.realive.dto.order.OrderDeliveryResponseDTO;
import com.realive.domain.order.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerOrderDeliveryRepository
        extends JpaRepository<OrderDelivery, Long>, SellerOrderDeliveryRepositoryCustom {

@Query("""
        SELECT d FROM OrderDelivery d
        JOIN d.order o
        JOIN o.orderItems oi
        JOIN oi.product p
        WHERE o.id = :orderId AND p.seller.id = :sellerId
    """)
    Optional<OrderDelivery> findByOrderIdAndSellerId(
            @Param("orderId") Long orderId,
            @Param("sellerId") Long sellerId
    );



    // ✅ 단건 조회 (orderId + sellerId) → DTO Projection
    @Query("""
        SELECT new com.realive.dto.order.OrderDeliveryResponseDTO(
            o.id,
            p.name,
            o.customer.id,
            d.status,
            d.startDate,
            d.completeDate,
            d.trackingNumber,
            d.carrier
        )
        FROM OrderDelivery d
        JOIN d.order o
        JOIN o.orderItems oi
        JOIN oi.product p
        WHERE o.id = :orderId
          AND p.seller.id = :sellerId
    """)
    Optional<OrderDeliveryResponseDTO> findDeliveryDTOByOrderIdAndSellerId(
            @Param("orderId") Long orderId,
            @Param("sellerId") Long sellerId
    );

    // ✅ 전체 조회 (sellerId 기준) → DTO Projection
    @Query("""
        SELECT new com.realive.dto.order.OrderDeliveryResponseDTO(
            o.id,
            p.name,
            o.customer.id,
            d.status,
            d.startDate,
            d.completeDate,
            d.trackingNumber,
            d.carrier
        )
        FROM OrderDelivery d
        JOIN d.order o
        JOIN o.orderItems oi
        JOIN oi.product p
        WHERE p.seller.id = :sellerId
    """)
    List<OrderDeliveryResponseDTO> findAllDeliveryDTOBySellerId(@Param("sellerId") Long sellerId);
}
