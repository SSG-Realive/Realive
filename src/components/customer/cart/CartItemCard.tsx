'use client';

import { CartItem } from '@/types/customer/cart/cart';
import Link from 'next/link';

interface Props {
    item: CartItem;
    onQuantityChange: (cartItemId: number, newQty: number) => void;
    onDelete: (cartItemId: number) => void;
    isSelected: boolean;
    onToggleSelect: (cartItemId: number) => void;
}

export default function CartItemCard({ item, onQuantityChange, onDelete, isSelected, onToggleSelect }: Props) {
    return (
        <li className="flex items-center bg-white rounded-lg shadow-sm p-4 relative">
            <input
                type="checkbox"
                className="w-5 h-5 mr-4 flex-shrink-0"
                checked={isSelected}
                onChange={() => onToggleSelect(item.cartItemId)}
            />

            <Link href={`/main/products/${item.productId}`} className="flex items-center flex-grow">
                <img
                    src={item.imageThumbnailUrl}
                    alt={item.productName}
                    className="w-24 h-24 object-cover rounded-md flex-shrink-0"
                />
                <div className="flex-grow ml-4">
                    {/* ✨ 판매자 이름 표시 부분 제거 */}
                    <h3 className="font-medium hover:underline leading-tight">{item.productName}</h3>
                    <p className="font-bold mt-1">{item.productPrice.toLocaleString()}원</p>
                    <div className="flex items-center mt-2">
                        <button
                            onClick={(e) => { e.preventDefault(); onQuantityChange(item.cartItemId, item.quantity - 1); }}
                            className="px-2 py-1 border rounded"
                        >
                            -
                        </button>
                        <span className="mx-3">{item.quantity}</span>
                        <button
                            onClick={(e) => { e.preventDefault(); onQuantityChange(item.cartItemId, item.quantity + 1); }}
                            className="px-2 py-1 border rounded"
                        >
                            +
                        </button>
                    </div>
                </div>
            </Link>
            <button
                onClick={(e) => { e.stopPropagation(); onDelete(item.cartItemId); }}
                className="absolute top-3 right-3 text-gray-400 hover:text-gray-600 text-2xl"
            >
                &times;
            </button>
        </li>
    );
}