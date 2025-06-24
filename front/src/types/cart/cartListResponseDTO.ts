import {CartItemResponseDTO} from "@/types/cart/cartItemResponseDTO";


export interface CartListResponseDTO {
    items: CartItemResponseDTO[];
    totalItems: number;
    totalCartPrice: number;
}