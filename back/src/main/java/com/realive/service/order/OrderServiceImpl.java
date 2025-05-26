package com.realive.service.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.DeliveryType;
import com.realive.domain.common.enums.MediaType;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.common.enums.PaymentType;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.DeliveryPolicy;
import com.realive.domain.product.Product;
import com.realive.dto.order.*;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderItemRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.product.DeliveryPolicyRepository;
import com.realive.repository.product.ProductImageRepository;
import com.realive.repository.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
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

    @Override
    public OrderResponseDTO getOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findByCustomerIdAndOrderId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 구매 내역입니다."));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        if (orderItems.isEmpty()) {
            throw new NoSuchElementException("주문 항목이 없습니다.");
        }

        List<OrderItemResponseDTO> itemDTOs = new ArrayList<>();
        int totalDeliveryFeeForOrder = 0;

        List<Long> productIdsInOrder = orderItems.stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> thumbnailUrls = productImageRepository.findThumbnailUrlsByProductIds(productIdsInOrder, MediaType.IMAGE)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (String) arr[1]
                ));


        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();

            String imageUrl = thumbnailUrls.get(product.getId());

            int itemDeliveryFee = 0;
            // ⭐ DeliveryPolicyRepository에 findByProduct_IdIn 메서드가 없으므로,
            // 현재는 findByProduct(Product)를 사용하거나, 모든 정책을 가져와 필터링해야 합니다.
            // 여기서는 N+1 문제를 감수하고 findByProduct를 사용합니다.
            // 성능 문제가 발생하면 DeliveryPolicyRepository에 해당 메서드를 추가해야 합니다.
            Optional<DeliveryPolicy> deliveryPolicyOptional = deliveryPolicyRepository.findByProduct(product);
            if (deliveryPolicyOptional.isPresent()) {
                DeliveryPolicy deliveryPolicy = deliveryPolicyOptional.get();
                if (deliveryPolicy.getType() == DeliveryType.유료배송) {
                    itemDeliveryFee = deliveryPolicy.getCost();
                }
            }
            totalDeliveryFeeForOrder += itemDeliveryFee;

            itemDTOs.add(OrderItemResponseDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .imageUrl(imageUrl)
                    .build());
        }

        return OrderResponseDTO.from(
                order,
                itemDTOs,
                totalDeliveryFeeForOrder
        );
    }


    @Override
    public Page<OrderResponseDTO> getOrderList(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllOrders(pageable);
        List<OrderResponseDTO> responseList = new ArrayList<>();

        List<Long> orderIds = orderPage.getContent().stream().map(Order::getId).collect(Collectors.toList());

        Map<Long, List<OrderItem>> orderItemsByOrderId = orderItemRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        List<Long> productIds = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(item -> item.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> thumbnailUrls = productImageRepository.findThumbnailUrlsByProductIds(productIds, MediaType.IMAGE)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (String) arr[1]
                ));

        // ⭐ DeliveryPolicyRepository에 findByProduct_IdIn 메서드가 없으므로,
        // 모든 배송 정책을 가져온 후 필터링하여 맵을 구성합니다.
        // 데이터가 많아지면 비효율적일 수 있습니다. (N+1 문제 발생 방지 목적)
        Map<Long, DeliveryPolicy> deliveryPoliciesByProductId = new HashMap<>();
        List<DeliveryPolicy> allDeliveryPolicies = deliveryPolicyRepository.findAll();
        for (DeliveryPolicy policy : allDeliveryPolicies) {
            // 조회된 상품 ID 목록에 해당하는 정책만 맵에 추가
            if (productIds.contains(policy.getProduct().getId())) {
                deliveryPoliciesByProductId.put(policy.getProduct().getId(), policy);
            }
        }


        for (Order order : orderPage.getContent()) {
            List<OrderItem> currentOrderItems = orderItemsByOrderId.getOrDefault(order.getId(), new ArrayList<>());
            List<OrderItemResponseDTO> itemDTOs = new ArrayList<>();
            int totalDeliveryFeeForOrder = 0;

            for (OrderItem item : currentOrderItems) {
                Product product = item.getProduct();
                String imageUrl = thumbnailUrls.get(product.getId());

                int itemDeliveryFee = 0;
                DeliveryPolicy deliveryPolicy = deliveryPoliciesByProductId.get(product.getId());
                if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송) {
                    itemDeliveryFee = deliveryPolicy.getCost();
                }
                totalDeliveryFeeForOrder += itemDeliveryFee;

                itemDTOs.add(OrderItemResponseDTO.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .imageUrl(imageUrl)
                        .build());
            }

            OrderResponseDTO orderDTO = OrderResponseDTO.fromOrder(order);
            orderDTO.setDeliveryFee(totalDeliveryFeeForOrder);
            orderDTO.setOrderItems(itemDTOs);
            responseList.add(orderDTO);
        }

        long totalElements = orderPage.getTotalElements();

        return new PageImpl<>(responseList, pageable, totalElements);
    }


    @Override
    @Transactional
    public void deleteOrder(OrderDeleteRequestDTO orderDeleteRequestDTO) {
        Long orderId = orderDeleteRequestDTO.getOrderId();
        Long customerId = orderDeleteRequestDTO.getCustomerId();

        Order order = orderRepository.findByCustomerIdAndOrderId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("삭제하려는 주문을 찾을 수 없습니다: 주문 ID " + orderId + ", 고객 ID " + customerId));

        if (!((order.getStatus() == OrderStatus.PAYMENT_COMPLETED || order.getStatus() == OrderStatus.ORDER_RECEIVED) &&
                order.getDeliveryStatus() == DeliveryStatus.DELIVERY_PREPARING)) {
            throw new IllegalStateException(String.format("주문 상태가 '%s' 또는 배송 상태가 '%s'이므로 삭제할 수 없습니다. 삭제 가능한 상태: 주문 상태 (%s, %s), 배송 상태 (%s)",
                    order.getStatus().getDescription(), order.getDeliveryStatus().getDescription(),
                    OrderStatus.PAYMENT_COMPLETED.getDescription(), OrderStatus.ORDER_RECEIVED.getDescription(),
                    DeliveryStatus.DELIVERY_PREPARING.getDescription()));
        }


        List<OrderItem> orderItemsToDelete = orderItemRepository.findByOrderId(order.getId());
        orderItemRepository.deleteAll(orderItemsToDelete);

        orderRepository.delete(order);
        log.info("주문이 성공적으로 삭제되었습니다: 주문 ID {}", orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(OrderCancelRequestDTO orderCancelRequestDTO) {
        Long orderId = orderCancelRequestDTO.getOrderId();
        Long customerId = orderCancelRequestDTO.getCustomerId();
        String reason = orderCancelRequestDTO.getReason();

        Order order = orderRepository.findByCustomerIdAndOrderId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("취소하려는 주문을 찾을 수 없습니다 : 주문 ID " + orderId + ", 고객 ID " + customerId));

        if (!((order.getStatus() == OrderStatus.PAYMENT_COMPLETED ||
                order.getStatus() == OrderStatus.ORDER_RECEIVED) &&
                order.getDeliveryStatus() == DeliveryStatus.DELIVERY_PREPARING)) {
            throw new IllegalStateException(String.format("현재 주문 상태가 '%s' 또는 배송 상태가 '%s'이므로 취소할 수 없습니다. 취소 가능한 상태: 주문 상태 (%s, %s), 배송 상태 (%s)",
                    order.getStatus().getDescription(), order.getDeliveryStatus().getDescription(),
                    OrderStatus.PAYMENT_COMPLETED.getDescription(), OrderStatus.ORDER_RECEIVED.getDescription(),
                    DeliveryStatus.DELIVERY_PREPARING.getDescription()));
        }

        order.setStatus(OrderStatus.PURCHASE_CANCELED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("주문 상태가 '구매취소'로 변경되었습니다: 주문 ID {}", orderId);
        if (reason != null && !reason.isEmpty()) {
            log.info("구매취소 사유: {}", reason);
        }
    }

    @Override
    @Transactional
    public void confirmOrder(OrderConfirmRequestDTO orderConfirmRequestDTO) {
        Long orderId = orderConfirmRequestDTO.getOrderId();
        Long customerId = orderConfirmRequestDTO.getCustomerId();

        Order order = orderRepository.findByCustomerIdAndOrderId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("구매확정 하려는 주문을 찾을 수 없습니다: 주문 ID " + orderId + ", 고객 ID " + customerId));

        if (!(order.getStatus() == OrderStatus.PAYMENT_COMPLETED &&
                order.getDeliveryStatus() == DeliveryStatus.DELIVERY_COMPLETED)) {
            throw new IllegalStateException(String.format("현재 주문 상태가 '%s' 또는 배송 상태가 '%s'이므로 구매확정할 수 없습니다. 구매확정은 '%s' 상태의 '%s' 주문만 가능합니다.",
                    order.getStatus().getDescription(), order.getDeliveryStatus().getDescription(),
                    OrderStatus.PAYMENT_COMPLETED.getDescription(), DeliveryStatus.DELIVERY_COMPLETED.getDescription()));
        }

        order.setStatus(OrderStatus.PURCHASE_CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("주문 상태가 '구매확정'으로 변경되었습니다: 주문 ID {}", orderId);
    }

    @Override
    @Transactional
    public Long processPayment(PayRequestDTO payRequestDTO) {
        Customer customer = customerRepository.findById(payRequestDTO.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + payRequestDTO.getCustomerId()));

        String receiverName = payRequestDTO.getReceiverName();
        String phone = payRequestDTO.getPhone();
        String deliveryAddress = payRequestDTO.getDeliveryAddress();
        PaymentType paymentType = payRequestDTO.getPaymentMethod();

        if (receiverName == null || receiverName.isEmpty() ||
                phone == null || phone.isEmpty() ||
                deliveryAddress == null || deliveryAddress.isEmpty() ||
                paymentType == null) {
            throw new IllegalArgumentException("필수 배송 및 결제 정보가 누락되었습니다.");
        }

        List<OrderItem> orderItemsToSave = new ArrayList<>();
        int calculatedTotalProductPrice = 0;
        int totalDeliveryFee = 0;

        if (payRequestDTO.getProductId() != null && payRequestDTO.getQuantity() != null && payRequestDTO.getQuantity() > 0) {
            Product product = productRepository.findById(payRequestDTO.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("결제하려는 상품을 찾을 수 없습니다: " + payRequestDTO.getProductId()));

            int itemPrice = product.getPrice();
            int itemQuantity = payRequestDTO.getQuantity();

            calculatedTotalProductPrice += itemPrice * itemQuantity;

            totalDeliveryFee += calculateDeliveryFeeForProduct(product);

            orderItemsToSave.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemQuantity)
                    .price(itemPrice)
                    .build());

        } else if (payRequestDTO.getOrderItems() != null && !payRequestDTO.getOrderItems().isEmpty()) {
            List<Long> processedProductIdsForDelivery = new ArrayList<>();

            for (ProductQuantityDTO itemDTO : payRequestDTO.getOrderItems()) {
                if (itemDTO.getProductId() == null || itemDTO.getQuantity() <= 0) {
                    throw new IllegalArgumentException("잘못된 주문 상품 정보가 포함되어 있습니다.");
                }

                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("결제하려는 상품을 찾을 수 없습니다: " + itemDTO.getProductId()));

                int itemPrice = product.getPrice();
                int itemQuantity = itemDTO.getQuantity();

                calculatedTotalProductPrice += itemPrice * itemQuantity;

                Optional<DeliveryPolicy> deliveryPolicyOptional = deliveryPolicyRepository.findByProduct(product);
                if (deliveryPolicyOptional.isPresent()) {
                    DeliveryPolicy policy = deliveryPolicyOptional.get();
                    if (policy.getType() == DeliveryType.유료배송 && !processedProductIdsForDelivery.contains(product.getId())) {
                        totalDeliveryFee += policy.getCost();
                        processedProductIdsForDelivery.add(product.getId());
                    }
                }
                orderItemsToSave.add(OrderItem.builder()
                        .product(product)
                        .quantity(itemQuantity)
                        .price(itemPrice)
                        .build());
            }
        } else {
            throw new IllegalArgumentException("결제할 상품 정보가 없습니다. productId/quantity 또는 orderItems 중 하나를 제공해야 합니다.");
        }

        int finalTotalPrice = calculatedTotalProductPrice + totalDeliveryFee;

        boolean paymentSuccess = processWithPaymentGateway(customer, finalTotalPrice, paymentType);
        if (!paymentSuccess) {
            throw new IllegalStateException("결제 처리 중 오류가 발생했거나 결제가 실패했습니다.");
        }

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PAYMENT_COMPLETED)
                .deliveryStatus(DeliveryStatus.DELIVERY_PREPARING)
                .totalPrice(finalTotalPrice)
                .deliveryAddress(deliveryAddress)
                .receiverName(receiverName)
                .phone(phone)
                .paymentType(paymentType.name())
                .OrderedAt(LocalDateTime.now())
                .UpdatedAt(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);

        for (OrderItem item : orderItemsToSave) {
            item.setOrder(order);
            orderItemRepository.save(item);
        }

        log.info("결제 성공 및 주문 생성 완료: 주문 ID {}", order.getId());
        return order.getId();
    }

    private int calculateDeliveryFeeForProduct(Product product) {
        return deliveryPolicyRepository.findByProduct(product)
                .map(policy -> (policy.getType() == DeliveryType.유료배송) ? policy.getCost() : 0)
                .orElse(0);
    }

    private boolean processWithPaymentGateway(Customer customer, int amount, PaymentType paymentType) {
        log.info("--- PG사(Payment Gateway) 결제 요청 시뮬레이션 ---");
        log.info("  고객 ID: {}", customer.getId());
        log.info("  결제 금액: {}원", amount);
        log.info("  결제 수단: {}", paymentType.getDescription());
        log.info("  PG사 결제 성공 (시뮬레이션)");
        return true;
    }
}