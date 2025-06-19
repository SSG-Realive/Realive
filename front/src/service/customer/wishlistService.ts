import apiClient from '@/lib/apiClient';
import { WishlistToggleRequest } from '@/types/customer/wishlist/wishlist';
import { ProductListDTO } from '@/types/seller/product/product';

// 찜 목록 조회
export async function fetchWishlist(): Promise<ProductListDTO[]> {
    const res = await apiClient.get('/customer/wishlist/my');
    return res.data;
}

// 찜 토글 (찜 추가/해제) → true: 찜됨, false: 찜 해제됨
export async function toggleWishlist(data: WishlistToggleRequest): Promise<boolean> {
    const res = await apiClient.post('/customer/wishlist/toggle', data);
    return res.data?.wishlist ?? false;
}
