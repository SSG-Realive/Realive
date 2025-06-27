package com.realive.service.order;

import com.realive.domain.common.enums.*;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.DeliveryPolicy;
import com.realive.domain.product.Product;
import com.realive.dto.order.*;
import com.realive.dto.payment.TossPaymentApproveRequestDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderDeliveryRepository;
import com.realive.repository.order.OrderItemRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.product.DeliveryPolicyRepository;
import com.realive.repository.product.ProductImageRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.service.payment.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional // Spring의 @Transactional 사용 권장 (org.springframework.transaction.annotation.Transactional)
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
    private final PaymentService paymentService; // PaymentService 주입

    // 구매내역 조회
    @Override
    public OrderResponseDTO getOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findByCustomerIdAndId(customerId, orderId)
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
                .filter(policy -> policy.getProduct() != null && productIdsInOrder.contains(policy.getProduct().getId()))
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
                    .id(orderItem.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .imageUrl(imageUrl)
                    .sellerId(product.getSeller().getId())
                    .build());
        }

        // OrderDelivery 정보 조회
        Optional<OrderDelivery> optionalOrderDelivery = orderDeliveryRepository.findByOrder(order);
        String currentDeliveryStatus = optionalOrderDelivery
                .map(delivery -> delivery.getStatus().getDescription())
                .orElse(DeliveryStatus.INIT.getDescription());
        String paymentType = order.getPaymentMethod(); // Order 엔티티에서 결제 방식 가져오기

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
    public Page<OrderResponseDTO> getOrderList(Pageable pageable, Long customerId) {
        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);
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
                .filter(policy -> policy.getProduct() != null && productIds.contains(policy.getProduct().getId()))
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
                        .id(item.getId())
                        .productId(product.getId())
                        .productName(product.getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .imageUrl(imageUrl)
                        .sellerId(product.getSeller().getId())
                        .build());
            }

            String currentDeliveryStatus = deliveryStatusByOrderId.getOrDefault(order.getId(), DeliveryStatus.INIT.getDescription());
            String paymentType = order.getPaymentMethod();

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

        Order order = orderRepository.findByCustomerIdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("삭제하려는 주문을 찾을 수 없습니다: 주문 ID " + orderId + ", 고객 ID " + customerId));

        Optional<OrderDelivery> optionalOrderDelivery = orderDeliveryRepository.findByOrder(order);

        if (optionalOrderDelivery.isPresent()) {
            DeliveryStatus deliveryStatus = optionalOrderDelivery.get().getStatus();
            if (!(deliveryStatus == DeliveryStatus.DELIVERY_PREPARING || deliveryStatus == DeliveryStatus.INIT)) {
                throw new IllegalStateException(String.format("현재 배송 상태가 '%s'이므로 주문을 삭제할 수 없습니다. '%s' 또는 '%s' 상태의 주문만 삭제 가능합니다.",
                        deliveryStatus.getDescription(),
                        DeliveryStatus.DELIVERY_PREPARING.getDescription(),
                        DeliveryStatus.INIT.getDescription()));
            }
        }

        if (!(order.getStatus() == OrderStatus.PAYMENT_CANCELED ||
                order.getStatus() == OrderStatus.PURCHASE_CANCELED ||
                order.getStatus() == OrderStatus.REFUND_COMPLETED ||
                order.getStatus() == OrderStatus.PURCHASE_CONFIRMED)) {
            throw new IllegalStateException(String.format("현재 주문 상태가 '%s'이므로 삭제할 수 없습니다.",
                    order.getStatus().getDescription()));
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

        Order order = orderRepository.findByCustomerIdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("취소하려는 주문을 찾을 수 없습니다 : 주문 ID " + orderId + ", 고객 ID " + customerId));

        Optional<OrderDelivery> optionalOrderDelivery = orderDeliveryRepository.findByOrder(order);

        if (optionalOrderDelivery.isPresent()) {
            DeliveryStatus deliveryStatus = optionalOrderDelivery.get().getStatus();
            if (!(deliveryStatus == DeliveryStatus.DELIVERY_PREPARING || deliveryStatus == DeliveryStatus.INIT)) {
                throw new IllegalStateException(String.format("현재 배송 상태가 '%s'이므로 주문을 취소할 수 없습니다. '%s' 또는 '%s' 상태의 주문만 취소 가능합니다.",
                        deliveryStatus.getDescription(),
                        DeliveryStatus.DELIVERY_PREPARING.getDescription(),
                        DeliveryStatus.INIT.getDescription()));
            }

            // 배송 취소 시 completeDate를 현재 시간으로 설정
            optionalOrderDelivery.get().setCompleteDate(LocalDateTime.now());
            orderDeliveryRepository.save(optionalOrderDelivery.get());
        }

        if (!(order.getStatus() == OrderStatus.PAYMENT_COMPLETED ||
                order.getStatus() == OrderStatus.ORDER_RECEIVED || order.getStatus() == OrderStatus.INIT)) {
            throw new IllegalStateException(String.format("현재 주문 상태가 '%s'이므로 취소할 수 없습니다. 취소 가능한 상태: (%s, %s, %s)",
                    order.getStatus().getDescription(),
                    OrderStatus.PAYMENT_COMPLETED.getDescription(), OrderStatus.ORDER_RECEIVED.getDescription(), OrderStatus.INIT.getDescription()));
        }

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

        Order order = orderRepository.findByCustomerIdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("구매확정 하려는 주문을 찾을 수 없습니다: 주문 ID " + orderId + ", 고객 ID " + customerId));

        OrderDelivery orderDelivery = orderDeliveryRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalStateException("배송 정보가 없는 주문은 구매 확정할 수 없습니다."));

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

    // 단일 상품 바로 구매 결제 진행 로직
    @Override
    @Transactional
    public Long processDirectPayment(PayRequestDTO payRequestDTO) {
        log.info("단일 상품 바로 구매 결제 처리 시작: {}", payRequestDTO);

        // **유효성 검증: 단일 상품 결제에 필요한 필드 확인**
        if (payRequestDTO.getProductId() == null || payRequestDTO.getQuantity() == null || payRequestDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("단일 상품 결제에는 productId와 quantity가 필수이며, 수량은 1개 이상이어야 합니다.");
        }
        if (payRequestDTO.getOrderItems() != null && !payRequestDTO.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("단일 상품 결제 시에는 orderItems를 포함할 수 없습니다.");
        }

        // 고객 정보 조회
        Customer customer = customerRepository.findById(payRequestDTO.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + payRequestDTO.getCustomerId()));

        // 배송 및 결제 정보 조회 (공통)
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

        // 상품 정보 조회
        Product product = productRepository.findById(payRequestDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("결제하려는 상품을 찾을 수 없습니다: ID " + payRequestDTO.getProductId()));

        // DeliveryPolicyRepository 수정 권한이 없으므로 findAll 후 필터링하여 찾음
        DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> policy.getProduct() != null && policy.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        // TODO: 재고 확인 및 감소 로직 추가
        // if (product.getStock() < payRequestDTO.getQuantity()) {
        //     throw new IllegalStateException("상품 재고가 부족합니다.");
        // }
        // product.decreaseStock(payRequestDTO.getQuantity());
        // productRepository.save(product); // 재고 감소 후 저장

        calculatedTotalProductPrice += product.getPrice() * payRequestDTO.getQuantity();

        if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송) {
            totalDeliveryFee += deliveryPolicy.getCost();
        }

        orderItemsToSave.add(OrderItem.builder()
                .product(product)
                .quantity(payRequestDTO.getQuantity())
                .price(product.getPrice()) // 주문 당시 상품 가격 저장
                .build());

        // 최종 결제 금액 계산
        int finalTotalPrice = calculatedTotalProductPrice + totalDeliveryFee;

        // ------------------ 실제 토스페이먼츠 결제 승인 요청 ------------------
        // PayRequestDTO에서 받은 paymentKey와 tossOrderId를 사용
        TossPaymentApproveRequestDTO tossApproveRequest = TossPaymentApproveRequestDTO.builder()
                .paymentKey(payRequestDTO.getPaymentKey())
                .orderId(payRequestDTO.getTossOrderId()) // 토스페이먼츠 위젯에서 받은 orderId 사용
                .amount((long) finalTotalPrice) // Long 타입으로 캐스팅
                .build();

        try {
            // PaymentService를 통해 토스페이먼츠 API 결제 승인 요청
            // 이 호출이 성공하면 결제가 최종적으로 완료된 것임.
            paymentService.approveTossPayment(tossApproveRequest);
            log.info("토스페이먼츠 결제 승인 성공: paymentKey={}, orderId={}", payRequestDTO.getPaymentKey(), payRequestDTO.getTossOrderId());
        } catch (ResponseStatusException e) {
            log.error("토스페이먼츠 API 통신 중 HTTP 오류 발생: {}. 결제 실패", e.getReason(), e);
            throw new IllegalStateException("결제 처리 중 외부 API 오류가 발생했습니다: " + e.getReason(), e);
        } catch (IllegalArgumentException e) {
            log.error("토스페이먼츠 결제 응답 유효성 검증 실패 또는 비즈니스 로직 오류: {}. 결제 실패", e.getMessage(), e);
            throw new IllegalStateException("결제 데이터 불일치 또는 비정상 상태: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 중 예기치 않은 오류 발생: {}. 결제 실패", e.getMessage(), e);
            throw new RuntimeException("결제 처리 중 알 수 없는 오류가 발생했습니다.", e);
        }
        // --------------------------------------------------------------------

        // ------------------ 결제 성공 후 DB에 주문 정보 저장 ------------------
        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PAYMENT_COMPLETED) // 주문 상태는 결제 완료
                .totalPrice(finalTotalPrice)
                .deliveryAddress(deliveryAddress)
                .orderedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .paymentMethod(paymentType.getDescription()) // PaymentType Enum의 설명을 저장
                .build();
        order = orderRepository.save(order);

        for (OrderItem item : orderItemsToSave) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(orderItemsToSave); // 모든 주문 항목 한 번에 저장

        // OrderDelivery 상태를 INIT으로 설정
        OrderDelivery orderDelivery = OrderDelivery.builder()
                .order(order)
                .status(DeliveryStatus.INIT)
                .startDate(LocalDateTime.now())
                .build();
        orderDeliveryRepository.save(orderDelivery);

        log.info("단일 상품 주문 성공 및 생성 완료: 주문 ID {}", order.getId());
        return order.getId();
    }

    @Override
    public DirectPaymentInfoDTO getDirectPaymentInfo(Long productId, Integer quantity) {
        log.info("단일 상품 바로 구매 정보 조회: productId={}, quantity={}", productId, quantity);

        // 상품 정보 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: ID " + productId));

        // 배송 정책 조회
        DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> policy.getProduct() != null && policy.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        // 배송비 계산
        int deliveryFee = 0;
        if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송) {
            deliveryFee = deliveryPolicy.getCost();
        }

        // 상품 이미지 URL 조회
        String imageUrl = productImageRepository.findThumbnailUrlsByProductIds(Collections.singletonList(productId), MediaType.IMAGE)
                .stream()
                .findFirst()
                .map(arr -> (String) arr[1])
                .orElse(null);

        // 총 상품 가격 계산
        int totalProductPrice = product.getPrice() * quantity;
        int totalPrice = totalProductPrice + deliveryFee;

        return DirectPaymentInfoDTO.builder()
                .productId(productId)
                .productName(product.getName())
                .quantity(quantity)
                .price(product.getPrice())
                .totalPrice(totalPrice)
                .deliveryFee(deliveryFee)
                .imageUrl(imageUrl)
                .build();
    }
}