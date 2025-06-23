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
export async function updateCartItemQuantity(cartItemId: number, quantity: number): Promise<void> {
    await apiClient.patch(`/customer/cart/${cartItemId}`, {
        quantity,
    });
}

// ✅ 장바구니 항목 삭제
export async function deleteCartItem(cartItemId: number): Promise<void> {
    await apiClient.delete(`/customer/cart/${cartItemId}`);
}

// ✅ 장바구니에 상품 추가
export async function addToCart(data: CartItemAddRequest): Promise<void> {
    await apiClient.post('/customer/cart', data);
}
