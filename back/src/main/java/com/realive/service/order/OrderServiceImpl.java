package com.realive.service.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.DeliveryType;
import com.realive.domain.common.enums.MediaType;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.common.enums.PaymentType;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.order.SellerOrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.DeliveryPolicy;
import com.realive.domain.product.Product;
import com.realive.dto.order.*;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.SellerOrderDeliveryRepository;
import com.realive.repository.order.OrderItemRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.product.DeliveryPolicyRepository;
import com.realive.repository.product.ProductImageRepository;
import com.realive.repository.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final DeliveryPolicyRepository deliveryPolicyRepository;
    private final CustomerRepository customerRepository;
    private final SellerOrderDeliveryRepository orderDeliveryRepository;

    @Override
    public OrderResponseDTO getOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findByCustomer_IdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 구매 내역입니다."));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        if (orderItems.isEmpty()) throw new NoSuchElementException("주문 항목이 없습니다.");

        List<Long> productIdsInOrder = orderItems.stream().map(i -> i.getProduct().getId()).distinct().toList();

        Map<Long, String> thumbnailUrls = productImageRepository.findThumbnailUrlsByProductIds(productIdsInOrder, MediaType.IMAGE)
                .stream().collect(Collectors.toMap(arr -> (Long) arr[0], arr -> (String) arr[1]));

        Map<Long, DeliveryPolicy> deliveryPolicies = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> productIdsInOrder.contains(policy.getProduct().getId()))
                .collect(Collectors.toMap(policy -> policy.getProduct().getId(), Function.identity()));

        List<OrderItemResponseDTO> itemDTOs = new ArrayList<>();
        int totalDeliveryFee = 0;
        Set<Long> processedProductIds = new HashSet<>();

        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            String imageUrl = thumbnailUrls.getOrDefault(product.getId(), null);
            int itemDeliveryFee = 0;

            DeliveryPolicy policy = deliveryPolicies.get(product.getId());
            if (policy != null && policy.getType() == DeliveryType.유료배송 && !processedProductIds.contains(product.getId())) {
                itemDeliveryFee = policy.getCost();
                totalDeliveryFee += itemDeliveryFee;
                processedProductIds.add(product.getId());
            }

            itemDTOs.add(OrderItemResponseDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .imageUrl(imageUrl)
                    .build());
        }

        Optional<SellerOrderDelivery> deliveryOpt = orderDeliveryRepository.findByOrder(order);
        String currentDeliveryStatus = deliveryOpt.map(d -> d.getDeliveryStatus().getDescription())
                .orElse(DeliveryStatus.DELIVERY_PREPARING.getDescription());

        return OrderResponseDTO.from(order, itemDTOs, totalDeliveryFee, "CARD", currentDeliveryStatus);
    }

    @Override
    @Transactional
    public void deleteOrder(OrderDeleteRequestDTO dto) {
        Order order = orderRepository.findByCustomer_IdAndId(dto.getCustomerId(), dto.getOrderId())
                .orElseThrow(() -> new NoSuchElementException("삭제하려는 주문을 찾을 수 없습니다."));

        orderDeliveryRepository.findByOrder(order).ifPresent(delivery -> {
            if (delivery.getDeliveryStatus() != DeliveryStatus.DELIVERY_PREPARING)
                throw new IllegalStateException("결제완료 상태에서만 삭제할 수 있습니다.");
        });

        if (!(order.getStatus() == OrderStatus.PAYMENT_COMPLETED || order.getStatus() == OrderStatus.ORDER_RECEIVED))
            throw new IllegalStateException("결제완료 또는 주문접수 상태에서만 삭제할 수 있습니다.");

        orderItemRepository.deleteAll(orderItemRepository.findByOrderId(order.getId()));
        orderDeliveryRepository.findByOrder(order).ifPresent(orderDeliveryRepository::delete);
        orderRepository.delete(order);
        log.info("주문 삭제 완료: {}", order.getId());
    }

    @Override
    @Transactional
    public void cancelOrder(OrderCancelRequestDTO dto) {
        Order order = orderRepository.findByCustomer_IdAndId(dto.getCustomerId(), dto.getOrderId())
                .orElseThrow(() -> new NoSuchElementException("취소하려는 주문을 찾을 수 없습니다."));

        orderDeliveryRepository.findByOrder(order).ifPresent(delivery -> {
            if (delivery.getDeliveryStatus() != DeliveryStatus.DELIVERY_PREPARING)
                throw new IllegalStateException("결제완료 상태에서만 취소할 수 있습니다.");
            delivery.setCompleteDate(LocalDateTime.now());
            orderDeliveryRepository.save(delivery);
        });

        order.setStatus(OrderStatus.PURCHASE_CANCELED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("주문 취소 완료: {}", order.getId());
    }

    @Override
    @Transactional
    public void confirmOrder(OrderConfirmRequestDTO dto) {
        Order order = orderRepository.findByCustomer_IdAndId(dto.getCustomerId(), dto.getOrderId())
                .orElseThrow(() -> new NoSuchElementException("구매확정 주문을 찾을 수 없습니다."));

        SellerOrderDelivery delivery = orderDeliveryRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalStateException("배송 정보가 없습니다."));

        if (delivery.getDeliveryStatus() != DeliveryStatus.DELIVERY_COMPLETED)
            throw new IllegalStateException("배송완료 상태에서만 구매확정 가능합니다.");

        order.setStatus(OrderStatus.PURCHASE_CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("구매확정 완료: {}", order.getId());
    }

    @Override
    @Transactional
    public Long processPayment(PayRequestDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));

        List<OrderItem> orderItems = new ArrayList<>();
        List<Long> productIds = new ArrayList<>();
        if (dto.getProductId() != null) productIds.add(dto.getProductId());
        if (dto.getOrderItems() != null) dto.getOrderItems().forEach(i -> productIds.add(i.getProductId()));

        Map<Long, Product> products = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        Map<Long, DeliveryPolicy> deliveryPolicies = deliveryPolicyRepository.findAll().stream()
                .filter(p -> productIds.contains(p.getProduct().getId()))
                .collect(Collectors.toMap(p -> p.getProduct().getId(), Function.identity()));

        int totalPrice = 0;
        int deliveryFee = 0;
        Set<Long> deliveryTracked = new HashSet<>();

        if (dto.getProductId() != null && dto.getQuantity() != null) {
            Product product = products.get(dto.getProductId());
            totalPrice += product.getPrice() * dto.getQuantity();

            if (deliveryPolicies.containsKey(product.getId()) && deliveryPolicies.get(product.getId()).getType() == DeliveryType.유료배송)
                deliveryFee += deliveryPolicies.get(product.getId()).getCost();

            orderItems.add(OrderItem.builder()
                    .product(product)
                    .quantity(dto.getQuantity())
                    .price(product.getPrice())
                    .build());
        } else if (dto.getOrderItems() != null) {
            for (ProductQuantityDTO item : dto.getOrderItems()) {
                Product product = products.get(item.getProductId());
                totalPrice += product.getPrice() * item.getQuantity();

                if (deliveryPolicies.containsKey(product.getId())
                        && deliveryPolicies.get(product.getId()).getType() == DeliveryType.유료배송
                        && !deliveryTracked.contains(product.getId())) {
                    deliveryFee += deliveryPolicies.get(product.getId()).getCost();
                    deliveryTracked.add(product.getId());
                }

                orderItems.add(OrderItem.builder()
                        .product(product)
                        .quantity(item.getQuantity())
                        .price(product.getPrice())
                        .build());
            }
        }

        int finalPrice = totalPrice + deliveryFee;
        if (!processWithPaymentGateway(customer, finalPrice, dto.getPaymentMethod()))
            throw new IllegalStateException("결제 실패");

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PAYMENT_COMPLETED)
                .totalPrice(finalPrice)
                .deliveryAddress(dto.getDeliveryAddress())
                .OrderedAt(LocalDateTime.now())
                .UpdatedAt(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
            orderItemRepository.save(item);
        }

        SellerOrderDelivery delivery = SellerOrderDelivery.builder()
                .order(order)
                .deliveryStatus(DeliveryStatus.DELIVERY_PREPARING)
                .startDate(LocalDateTime.now())
                .build();
        orderDeliveryRepository.save(delivery);

        log.info("결제 및 주문 완료: {}", order.getId());
        return order.getId();
    }

    private boolean processWithPaymentGateway(Customer customer, int amount, PaymentType paymentType) {
        log.info("PG 결제 시뮬레이션 - 고객ID: {}, 금액: {}, 수단: {}", customer.getId(), amount, paymentType.getDescription());
        return true;
    }
}
