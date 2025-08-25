import apiClient from '@/lib/apiClient';
import {
    CartItem,
    CartItemAddRequest,
    CartListResponse,
} from '@/types/customer/cart/cart';
import { useCartStore } from '@/store/customer/useCartStore';

// ✅ 장바구니 전체 조회 + Zustand에 수량 저장
export async function fetchCartList(): Promise<CartItem[]> {
    const res = await apiClient.get<CartListResponse>('/customer/cart');
    const items = res.data.items;

    // ✅ 수량 저장
    const { setItemCount } = useCartStore.getState();
    setItemCount(items.length);

    return items;
}

// ✅ 수량 변경
interface UpdateCartItemQuantityPayload {
    cartItemId: number;
    quantity: number;
}
export async function updateCartItemQuantity(
    payload: UpdateCartItemQuantityPayload
): Promise<void> {
    await apiClient.patch(`/customer/cart/${payload.cartItemId}`, {
        quantity: payload.quantity,
    });

    // ✅ 변경 후 장바구니 다시 조회하여 수량 갱신
    await fetchCartList();
}

// ✅ 장바구니 항목 삭제
interface DeleteCartItemPayload {
    cartItemId: number;
}
export async function deleteCartItem(payload: DeleteCartItemPayload): Promise<void> {
    await apiClient.delete(`/customer/cart/${payload.cartItemId}`);

    // ✅ 삭제 후 수량 갱신
    await fetchCartList();
}

// ✅ 장바구니에 상품 추가
export async function addToCart(data: CartItemAddRequest): Promise<void> {
    await apiClient.post('/customer/cart', data);

    // ✅ 추가 후 수량 갱신
    await fetchCartList();
}
