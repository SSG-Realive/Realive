package com.realive.service.cart.view;

import com.realive.domain.customer.CartItem;
import com.realive.dto.cart.CartItemResponseDTO;
import com.realive.dto.cart.CartListResponseDTO;
import com.realive.dto.productview.ProductListDto; // ProductListDto 임포트
import com.realive.dto.productview.ProductResponseDto; // ProductResponseDto 임포트
import com.realive.repository.cart.CartItemRepository;
import com.realive.repository.productview.ProductViewRepository; // ProductViewRepository 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CartViewServiceImpl implements CartViewService {

    private final CartItemRepository cartItemRepository;
    private final ProductViewRepository productViewRepository; // ProductViewRepository 사용

    @Override
    @Transactional(readOnly = true)
    public CartListResponseDTO getCart(Long customerId) {
        log.info("Fetching cart for customerId: {}", customerId);

        List<CartItem> cartItems = cartItemRepository.findByCustomerId(customerId);

        List<Long> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());

        // ProductViewRepository의 findProductDetailById를 사용하여 각 상품의 상세 DTO를 조회
        // N+1 쿼리를 피하기 위해 productIds 리스트를 사용하는 batch 조회는 ProductViewRepository에서 제공하지 않으므로,
        // 각 productId마다 개별적으로 호출해야 할 수 있습니다.
        // ProductViewRepository가 목록 조회 시 필요한 최소한의 정보를 제공하는 productList 메서드를 가지고 있으므로,
        // 이를 활용하거나, ProductDetailImpl에 여러 ID를 조회하는 메서드를 추가할 수 있습니다.
        // 현재는 상세 조회를 위해 findProductDetailById를 개별 호출하는 방식으로 구성합니다.
        // 성능이 중요해지면 ProductViewRepository에 `Map<Long, ProductResponseDto> findProductDetailsByIds(List<Long> ids);`
        // 와 같은 batch 조회 메서드를 추가하는 것을 고려해야 합니다.

        // ProductListDto를 활용하는 경우 (가볍게 목록 정보만 필요한 경우)
        // 여기서는 CartItemResponseDTO가 ProductResponseDto를 기반으로 만들어지므로, findProductDetailById를 사용합니다.
        // 만약 ProductListDto만으로 충분하다면 productViewRepository.productList를 활용할 수도 있습니다.

        Map<Long, ProductResponseDto> productDetailMap = productIds.stream()
                .map(id -> productViewRepository.findProductDetailById(id).orElse(null))
                .filter(dto -> dto != null)
                .collect(Collectors.toMap(ProductResponseDto::getId, dto -> dto));

        List<CartItemResponseDTO> itemDTOs = cartItems.stream()
                .map(cartItem -> {
                    ProductResponseDto productDetailDto = productDetailMap.get(cartItem.getProductId());

                    if (productDetailDto == null) {
                        log.warn("Product detail not found for cart item productId: {}", cartItem.getProductId());
                        return CartItemResponseDTO.builder()
                                .cartItemId(cartItem.getId())
                                .productId(cartItem.getProductId())
                                .productName("[상품 없음]")
                                .quantity(cartItem.getQuantity())
                                .productPrice(0)
                                .productImage(null)
                                .totalPrice(0)
                                .cartCreatedAt(cartItem.getCreatedAt())
                                .build();
                    }
                    return CartItemResponseDTO.from(cartItem, productDetailDto);
                })
                .collect(Collectors.toList());

        CartListResponseDTO cartResponse = CartListResponseDTO.from(itemDTOs);

        return cartResponse;
    }
}