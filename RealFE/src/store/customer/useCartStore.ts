import { create } from 'zustand';
import type { CartItem } from '@/types/customer/cart/cart';

interface CartState {
    itemsForCheckout: CartItem[];
    setItemsForCheckout: (items: CartItem[]) => void;

    itemCount: number;
    setItemCount: (count: number) => void;
}

export const useCartStore = create<CartState>((set) => ({
    itemsForCheckout: [],
    setItemsForCheckout: (items) => set({ itemsForCheckout: items }),

    itemCount: 0,
    setItemCount: (count) => set({ itemCount: count }),
}));