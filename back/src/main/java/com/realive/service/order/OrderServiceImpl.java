package com.realive.service.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.DeliveryType;
import com.realive.domain.common.enums.MediaType;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.common.enums.PaymentType;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.DeliveryPolicy;
import com.realive.domain.product.Product;
import com.realive.dto.order.*;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderDeliveryRepository;
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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
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
    private final OrderDeliveryRepository orderDeliveryRepository;

    // 구매내역 조회
    @Override
    public OrderResponseDTO getOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findByCustomer_IdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 구매 내역입니다. (주문 ID: " + orderId + ", 고객 ID: " + customerId + ")"));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        if (orderItems.isEmpty()) {
            throw new NoSuchElementException("주문 항목이 없습니다.");
        }

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

        // DeliveryPolicyRepository에 findByProductIds가 없으므로 findAll 후 필터링
        Map<Long, DeliveryPolicy> deliveryPoliciesByProductId = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> productIdsInOrder.contains(policy.getProduct().getId()))
                .collect(Collectors.toMap(policy -> policy.getProduct().getId(), Function.identity()));


        List<OrderItemResponseDTO> itemDTOs = new ArrayList<>();
        int totalDeliveryFeeForOrder = 0;
        List<Long> processedProductIdsForDelivery = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();

            String imageUrl = thumbnailUrls.getOrDefault(product.getId(), null);

            int itemDeliveryFee = 0;
            DeliveryPolicy deliveryPolicy = deliveryPoliciesByProductId.get(product.getId());

            if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송 && !processedProductIdsForDelivery.contains(product.getId())) {
                itemDeliveryFee = deliveryPolicy.getCost();
                totalDeliveryFeeForOrder += itemDeliveryFee;
                processedProductIdsForDelivery.add(product.getId());
            }

            itemDTOs.add(OrderItemResponseDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .imageUrl(imageUrl)
                    .build());
        }

        // OrderDelivery 정보 조회
        Optional<OrderDelivery> optionalOrderDelivery = orderDeliveryRepository.findByOrder(order);
        String currentDeliveryStatus = optionalOrderDelivery
                .map(delivery -> delivery.getStatus().getDescription())
                .orElse(DeliveryStatus.DELIVERY_PREPARING.getDescription());
        String paymentType = "CARD"; // 다른 결제수단은 없음

        return OrderResponseDTO.from(
                order,
                itemDTOs,
                totalDeliveryFeeForOrder,
                paymentType,
                currentDeliveryStatus
        );
    }

    // 구매 내역 리스트 조회
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

        // DeliveryPolicyRepository에 findByProductIds가 없으므로 findAll 후 필터링
        Map<Long, DeliveryPolicy> deliveryPoliciesByProductId = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> productIds.contains(policy.getProduct().getId()))
                .collect(Collectors.toMap(policy -> policy.getProduct().getId(), Function.identity()));

        Map<Long, String> deliveryStatusByOrderId = orderDeliveryRepository.findByOrderIn(orderPage.getContent()).stream()
                .collect(Collectors.toMap(
                        delivery -> delivery.getOrder().getId(),
                        delivery -> delivery.getStatus().getDescription(),
                        (existing, replacement) -> existing
                ));

        for (Order order : orderPage.getContent()) {
            List<OrderItem> currentOrderItems = orderItemsByOrderId.getOrDefault(order.getId(), new ArrayList<>());
            List<OrderItemResponseDTO> itemDTOs = new ArrayList<>();
            int totalDeliveryFeeForOrder = 0;
            List<Long> processedProductIdsForOrderListDelivery = new ArrayList<>();

            for (OrderItem item : currentOrderItems) {
                Product product = item.getProduct();
                String imageUrl = thumbnailUrls.getOrDefault(product.getId(), null);

                int itemDeliveryFee = 0;
                DeliveryPolicy deliveryPolicy = deliveryPoliciesByProductId.get(product.getId());
                if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송 && !processedProductIdsForOrderListDelivery.contains(product.getId())) {
                    itemDeliveryFee = deliveryPolicy.getCost();
                    totalDeliveryFeeForOrder += itemDeliveryFee;
                    processedProductIdsForOrderListDelivery.add(product.getId());
                }

                itemDTOs.add(OrderItemResponseDTO.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .imageUrl(imageUrl)
                        .build());
            }

            String currentDeliveryStatus = deliveryStatusByOrderId.getOrDefault(order.getId(), DeliveryStatus.DELIVERY_PREPARING.getDescription()); // 현재 enum에 UNKNOWN 없음, 기본값으로 '배송준비중' 설정
            String paymentType = "CARD"; // PaymentType은 1개로 통일

            OrderResponseDTO orderDTO = OrderResponseDTO.from(
                    order,
                    itemDTOs,
                    totalDeliveryFeeForOrder,
                    paymentType,
                    currentDeliveryStatus
            );
            responseList.add(orderDTO);
        }

        long totalElements = orderPage.getTotalElements();

        return new PageImpl<>(responseList, pageable, totalElements);
    }

    // 구매 내역 삭제
    @Override
    @Transactional
    public void deleteOrder(OrderDeleteRequestDTO orderDeleteRequestDTO) {
        Long orderId = orderDeleteRequestDTO.getOrderId();
        Long customerId = orderDeleteRequestDTO.getCustomerId();

        Order order = orderRepository.findByCustomer_IdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("삭제하려는 주문을 찾을 수 없습니다: 주문 ID " + orderId + ", 고객 ID " + customerId));

        Optional<OrderDelivery> optionalOrderDelivery = orderDeliveryRepository.findByOrder(order);

        // 주문이 존재할때 배송 상태를 조회
        if (optionalOrderDelivery.isPresent()) {
            DeliveryStatus deliveryStatus = optionalOrderDelivery.get().getStatus();
            //  DELIVERY_PREPARING 상태에서만 삭제 허용
            if (!(deliveryStatus == DeliveryStatus.DELIVERY_PREPARING)) {
                throw new IllegalStateException(String.format("현재 배송 상태가 '%s'이므로 주문을 삭제할 수 없습니다. '%s' 상태의 주문만 삭제 가능합니다.",
                        deliveryStatus.getDescription(),
                        DeliveryStatus.DELIVERY_PREPARING.getDescription()));
            }
        }

        // 주문 상태 확인: 결제 완료 또는 주문 접수 상태만 삭제 가능
        if (!(order.getStatus() == OrderStatus.PAYMENT_COMPLETED || order.getStatus() == OrderStatus.ORDER_RECEIVED)) {
            throw new IllegalStateException(String.format("현재 주문 상태가 '%s'이므로 삭제할 수 없습니다. 삭제 가능한 상태: (%s, %s)",
                    order.getStatus().getDescription(),
                    OrderStatus.PAYMENT_COMPLETED.getDescription(), OrderStatus.ORDER_RECEIVED.getDescription()));
        }

        List<OrderItem> orderItemsToDelete = orderItemRepository.findByOrderId(order.getId());
        orderItemRepository.deleteAll(orderItemsToDelete);

        optionalOrderDelivery.ifPresent(orderDeliveryRepository::delete);

        orderRepository.delete(order);
        log.info("주문이 성공적으로 삭제되었습니다: 주문 ID {}", orderId);
    }

    // 구매 취소
    @Override
    @Transactional
    public void cancelOrder(OrderCancelRequestDTO orderCancelRequestDTO) {
        Long orderId = orderCancelRequestDTO.getOrderId();
        Long customerId = orderCancelRequestDTO.getCustomerId();
        String reason = orderCancelRequestDTO.getReason();

        Order order = orderRepository.findByCustomer_IdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("취소하려는 주문을 찾을 수 없습니다 : 주문 ID " + orderId + ", 고객 ID " + customerId));

        Optional<OrderDelivery> optionalOrderDelivery = orderDeliveryRepository.findByOrder(order);

        // 주문이 존재할때 배송 상태를 조회
        if (optionalOrderDelivery.isPresent()) {
            DeliveryStatus deliveryStatus = optionalOrderDelivery.get().getStatus();
            // DELIVERY_PREPARING 상태에서만 취소 허용.
            if (!(deliveryStatus == DeliveryStatus.DELIVERY_PREPARING)) {
                throw new IllegalStateException(String.format("현재 배송 상태가 '%s'이므로 주문을 취소할 수 없습니다. '%s' 상태의 주문만 취소 가능합니다.",
                        deliveryStatus.getDescription(),
                        DeliveryStatus.DELIVERY_PREPARING.getDescription()));
            }

            // 배송 정보가 있고 취소가 가능한 상태라면, 배송 완료 시간을 설정
            optionalOrderDelivery.get().setCompleteDate(LocalDateTime.now());
            orderDeliveryRepository.save(optionalOrderDelivery.get());
        }

        // 주문 상태를 보고 취소가 되는지 필터링
        if (!(order.getStatus() == OrderStatus.PAYMENT_COMPLETED ||
                order.getStatus() == OrderStatus.ORDER_RECEIVED)) {
            throw new IllegalStateException(String.format("현재 주문 상태가 '%s'이므로 취소할 수 없습니다. 취소 가능한 상태: (%s, %s)",
                    order.getStatus().getDescription(),
                    OrderStatus.PAYMENT_COMPLETED.getDescription(), OrderStatus.ORDER_RECEIVED.getDescription()));
        }

        // 구매 취소로 상태 변경
        order.setStatus(OrderStatus.PURCHASE_CANCELED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // TODO: 판매(product.stock) 수량 복구 로직 필요

        log.info("주문 상태가 '구매취소'로 변경되었습니다: 주문 ID {}", orderId);
        if (reason != null && !reason.isEmpty()) {
            log.info("구매취소 사유: {}", reason);
        }
    }

    // 구매 확정
    @Override
    @Transactional
    public void confirmOrder(OrderConfirmRequestDTO orderConfirmRequestDTO) {
        Long orderId = orderConfirmRequestDTO.getOrderId();
        Long customerId = orderConfirmRequestDTO.getCustomerId();

        Order order = orderRepository.findByCustomer_IdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("구매확정 하려는 주문을 찾을 수 없습니다: 주문 ID " + orderId + ", 고객 ID " + customerId));

        OrderDelivery orderDelivery = orderDeliveryRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalStateException("배송 정보가 없는 주문은 구매 확정할 수 없습니다."));

        // DELIVERY_COMPLETED 상태에서만 구매확정 허용
        if (orderDelivery.getStatus() != DeliveryStatus.DELIVERY_COMPLETED) {
            throw new IllegalStateException(String.format("현재 배송 상태가 '%s'이므로 구매확정할 수 없습니다. 구매확정은 '%s' 상태의 주문만 가능합니다.",
                    orderDelivery.getStatus().getDescription(),
                    DeliveryStatus.DELIVERY_COMPLETED.getDescription()));
        }

        order.setStatus(OrderStatus.PURCHASE_CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("주문 상태가 '구매확정'으로 변경되었습니다: 주문 ID {}", orderId);
    }

    // 결제
    @Override
    @Transactional
    public Long processPayment(PayRequestDTO payRequestDTO) {
        //고객 정보 조회
        Customer customer = customerRepository.findById(payRequestDTO.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + payRequestDTO.getCustomerId()));

        // 배송 및 결제 정보 조회
        String receiverName = payRequestDTO.getReceiverName();
        String phone = payRequestDTO.getPhone();
        String deliveryAddress = payRequestDTO.getDeliveryAddress();
        PaymentType paymentType = payRequestDTO.getPaymentMethod();

        // 누락시 에러 발생
        if (receiverName == null || receiverName.isEmpty() ||
                phone == null || phone.isEmpty() ||
                deliveryAddress == null || deliveryAddress.isEmpty() ||
                paymentType == null) {
            throw new IllegalArgumentException("필수 배송 및 결제 정보가 누락되었습니다.");
        }

        // 주문 항목을 저장할 리스트 선언과 계산용 변수 선언, 초기화
        List<OrderItem> orderItemsToSave = new ArrayList<>();
        int calculatedTotalProductPrice = 0;
        int totalDeliveryFee = 0;

        // 주문의 모든 productId를 저장
        List<Long> allProductIdsInRequest = new ArrayList<>();
        if (payRequestDTO.getProductId() != null) {
            allProductIdsInRequest.add(payRequestDTO.getProductId());
        }
        if (payRequestDTO.getOrderItems() != null) {
            payRequestDTO.getOrderItems().forEach(item -> allProductIdsInRequest.add(item.getProductId()));
        }

        Map<Long, Product> productsMap = productRepository.findAllById(allProductIdsInRequest).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // DeliveryPolicyRepository에 findByProductIds가 없으므로 findAll 후 필터링
        // 상품 id를 키로 DeliveryPolicy 저장
        Map<Long, DeliveryPolicy> deliveryPoliciesMap = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> allProductIdsInRequest.contains(policy.getProduct().getId()))
                .collect(Collectors.toMap(policy -> policy.getProduct().getId(), Function.identity()));


        // 단일 상품 결제 처리
        if (payRequestDTO.getProductId() != null && payRequestDTO.getQuantity() != null && payRequestDTO.getQuantity() > 0) {
            Product product = productsMap.get(payRequestDTO.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("결제하려는 상품을 찾을 수 없습니다: ID " + payRequestDTO.getProductId());
            }

            // 상품의 수량과 금액으로 총액 계산
            int itemPrice = product.getPrice();
            int itemQuantity = payRequestDTO.getQuantity();

            // TODO: 재고 확인 및 감소 로직 추가

            calculatedTotalProductPrice += itemPrice * itemQuantity;

            // 배송비 추가
            DeliveryPolicy deliveryPolicy = deliveryPoliciesMap.get(product.getId());
            if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송) {
                totalDeliveryFee += deliveryPolicy.getCost();
            }

            // 주문 생성 후 리스트에 추가
            orderItemsToSave.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemQuantity)
                    .price(itemPrice)
                    .build());

        }
        // 다수의 상품 결제 처리
        else if (payRequestDTO.getOrderItems() != null && !payRequestDTO.getOrderItems().isEmpty()) {
            List<Long> processedProductIdsForDelivery = new ArrayList<>();

            for (ProductQuantityDTO itemDTO : payRequestDTO.getOrderItems()) {
                Product product = productsMap.get(itemDTO.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("결제하려는 상품을 찾을 수 없습니다: ID " + itemDTO.getProductId());
                }

                // 상품의 수량과 금액으로 총액 계산
                int itemPrice = product.getPrice();
                int itemQuantity = itemDTO.getQuantity();

                // TODO: 재고 확인 및 감소 로직 추가

                calculatedTotalProductPrice += itemPrice * itemQuantity;

                // 유료 배송일경우 비용 추가, 중복된 상품일시 비용 추가 하지 않음
                DeliveryPolicy deliveryPolicy = deliveryPoliciesMap.get(product.getId());
                if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송 && !processedProductIdsForDelivery.contains(product.getId())) {
                    totalDeliveryFee += deliveryPolicy.getCost();
                    processedProductIdsForDelivery.add(product.getId());
                }
                // 주문 생성후 리스트에 추가
                orderItemsToSave.add(OrderItem.builder()
                        .product(product)
                        .quantity(itemQuantity)
                        .price(itemPrice)
                        .build());
            }
        } else {
            throw new IllegalArgumentException("결제할 상품 정보가 없습니다. productId/quantity 또는 orderItems 중 하나를 제공해야 합니다.");
        }

        // 상품 가격과 배송비를 합산해 최종 결제 금액 계산
        int finalTotalPrice = calculatedTotalProductPrice + totalDeliveryFee;

        // Payment Gateway와 연동해 결제 처리, 실패 시 예외 발생
        boolean paymentSuccess = processWithPaymentGateway(customer, finalTotalPrice, paymentType);
        if (!paymentSuccess) {
            throw new IllegalStateException("결제 처리 중 오류가 발생했거나 결제가 실패했습니다.");
        }

        // ------------------ 결제 성공 후 DB에 주문 정보 저장 ------------------
        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PAYMENT_COMPLETED)
                .totalPrice(finalTotalPrice)
                .deliveryAddress(deliveryAddress)
                .orderedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);

        // 주문 항목들에 주문 ID 연결 후 저장
        for (OrderItem item : orderItemsToSave) {
            item.setOrder(order);
            orderItemRepository.save(item);
        }

        OrderDelivery orderDelivery = OrderDelivery.builder()
                .order(order)
                .status(DeliveryStatus.DELIVERY_PREPARING)
                .startDate(LocalDateTime.now())
                .build();
        orderDeliveryRepository.save(orderDelivery);

        log.info("결제 성공 및 주문 생성 완료: 주문 ID {}", order.getId());
        return order.getId();
    }

    // 결제 처리를 하는 시뮬레이션 메서드
    private boolean processWithPaymentGateway(Customer customer, int amount, PaymentType paymentType) {
        log.info("--- PG사(Payment Gateway) 결제 요청 시뮬레이션 ---");
        log.info("  고객 ID: {}", customer.getId());
        log.info("  결제 금액: {}원", amount);
        log.info("  결제 수단: {}", paymentType.getDescription());
        log.info("  PG사 결제 성공 (시뮬레이션)");
        return true;
    }
}