import { create } from 'zustand';
import type { CartItem } from '@/types/customer/cart/cart';

interface CartState {
    // 결제를 위해 선택된 아이템들만 저장할 배열
    itemsForCheckout: CartItem[];
    // 이 배열을 업데이트하는 액션
    setItemsForCheckout: (items: CartItem[]) => void;
}

export const useCartStore = create<CartState>((set) => ({
    itemsForCheckout: [],
    setItemsForCheckout: (items) => set({ itemsForCheckout: items }),
}));