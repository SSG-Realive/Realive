import apiClient from '@/lib/apiClient';
import {
    CartItem,
    CartItemAddRequest,
    CartListResponse,
} from '@/types/customer/cart/cart';

// ✅ 장바구니 전체 조회
export async function fetchCartList(): Promise<CartItem[]> {
    const res = await apiClient.get<CartListResponse>('/customer/cart');
    return res.data.items;
}

// ✅ 수량 변경
// ✨ 수정: payload 객체를 인자로 받도록 변경
interface UpdateCartItemQuantityPayload {
    cartItemId: number;
    quantity: number;
}
export async function updateCartItemQuantity(payload: UpdateCartItemQuantityPayload): Promise<void> {
    await apiClient.patch(`/customer/cart/${payload.cartItemId}`, {
        quantity: payload.quantity,
    });
}

// ✅ 장바구니 항목 삭제
// ✨ 수정: payload 객체를 인자로 받도록 변경
interface DeleteCartItemPayload {
    cartItemId: number;
}
export async function deleteCartItem(payload: DeleteCartItemPayload): Promise<void> {
    await apiClient.delete(`/customer/cart/${payload.cartItemId}`);
}

// ✅ 장바구니에 상품 추가
export async function addToCart(data: CartItemAddRequest): Promise<void> {
    await apiClient.post('/customer/cart', data);
}