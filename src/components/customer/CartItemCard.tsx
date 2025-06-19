'use client';

import { CartItem } from '@/types/customer/cart/cart';
import Image from 'next/image';

interface Props {
    item: CartItem;
    onQuantityChange: (cartItemId: number, newQty: number) => void;
    onDelete: (cartItemId: number) => void;
}

export default function CartItemCard({ item, onQuantityChange, onDelete }: Props) {
    return (
        <li className="flex items-center justify-between border-b pb-4">
            <div className="flex items-center space-x-4">
                <img
                    src={item.imageThumbnailUrl}
                    alt={item.productName}
                    className="w-24 h-24 object-cover rounded"
                />
                <div>
                    <p className="font-semibold">{item.productName}</p>
                    <p className="text-green-600 font-bold">
                        {(item.productPrice * item.quantity).toLocaleString()}원
                    </p>
                    <div className="flex items-center mt-2 space-x-2">
                        <button
                            onClick={() => onQuantityChange(item.cartItemId, item.quantity - 1)}
                            className="px-2 border rounded"
                        >
                            -
                        </button>
                        <span>{item.quantity}</span>
                        <button
                            onClick={() => onQuantityChange(item.cartItemId, item.quantity + 1)}
                            className="px-2 border rounded"
                        >
                            +
                        </button>
                    </div>
                </div>
            </div>
            <button
                onClick={() => onDelete(item.cartItemId)}
                className="text-red-500 hover:underline"
            >
                삭제
            </button>
        </li>
    );
}
