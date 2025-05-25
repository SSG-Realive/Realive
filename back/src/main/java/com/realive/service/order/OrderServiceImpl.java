package com.realive.service.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.DeliveryType;
import com.realive.domain.common.enums.MediaType;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.customer.Customer; // Customer 클래스 임포트
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.DeliveryPolicy;
import com.realive.domain.product.Product;
import com.realive.dto.order.OrderAddRequestDTO;
import com.realive.dto.order.OrderItemResponseDTO;
import com.realive.dto.order.OrderListResponseDTO;
import com.realive.dto.order.OrderResponseDTO;
import com.realive.repository.customer.CustomerRepository; // CustomerRepository 임포트
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
public class OrderServiceImpl implements OrderService { // OrderService 인터페이스 구현

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final DeliveryPolicyRepository deliveryPolicyRepository;
    private final CustomerRepository customerRepository; // CustomerRepository 주입

    // line 40: `@Override` 어노테이션이 있어야 합니다.
    @Override
    @Transactional
    public Long createOrder(OrderAddRequestDTO orderAddRequestDTO) {
        Customer customer = customerRepository.findById(orderAddRequestDTO.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + orderAddRequestDTO.getCustomerId()));

        Product product = productRepository.findById(orderAddRequestDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + orderAddRequestDTO.getProductId()));

        String deliveryAddress = customer.getAddress();
        if (deliveryAddress == null || deliveryAddress.isEmpty()) {
            throw new IllegalArgumentException("고객의 배송 주소가 설정되어 있지 않습니다. 주문을 생성할 수 없습니다.");
        }

        int totalPrice = product.getPrice() * orderAddRequestDTO.getQuantity();

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PAYMENT_COMPLETED)
                .totalPrice(totalPrice)
                .deliveryAddress(deliveryAddress)
                .paymentType(orderAddRequestDTO.getPaymentType())
                .OrderedAt(LocalDateTime.now())
                .UpdatedAt(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(orderAddRequestDTO.getQuantity())
                .price(product.getPrice())
                .build();
        orderItemRepository.save(orderItem);

        return order.getId();
    }

    // line 88: `@Override` 어노테이션이 있어야 합니다.
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

        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();

            String imageUrl = productImageRepository.findFirstByProductIdAndIsThumbnailTrueAndMediaType(product.getId(), MediaType.IMAGE)
                    .map(img -> img.getUrl())
                    .orElse(null);

            int itemDeliveryFee = 0;
            // DeliveryPolicyRepository는 findByProduct(Product product)만 지원하므로, 개별 조회
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
                totalDeliveryFeeForOrder,
                DeliveryStatus.DELIVERY_PREPARING.getDescription()
        );
    }

    // line 138: `@Override` 어노테이션이 있어야 합니다.
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

        Map<Long, DeliveryPolicy> deliveryPoliciesByProductId = new HashMap<>();
        List<DeliveryPolicy> allDeliveryPolicies = deliveryPolicyRepository.findAll(); // findAll()로 모든 배송 정책 가져옴
        for (DeliveryPolicy policy : allDeliveryPolicies) {
            // Product 엔티티 수정 권한 없으므로, policy.getProduct().getId()로 직접 접근
            // 이는 DeliveryPolicy 엔티티가 Product에 대한 ManyToOne 관계를 가지고 있을 경우 작동
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
            orderDTO.setDeliveryStatus(DeliveryStatus.DELIVERY_PREPARING.getDescription());
            orderDTO.setOrderItems(itemDTOs);
            responseList.add(orderDTO);
        }

        long totalElements = orderPage.getTotalElements();

        return new PageImpl<>(responseList, pageable, totalElements);
    }
}